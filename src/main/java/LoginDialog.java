
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class LoginDialog extends JDialog {
    private JTextField txtIP, txtPort, txtUsername;
    private boolean confirmed = false;
    private BufferedImage originalImage;

    public LoginDialog(Frame parent) {
        super(parent, "Conectar al Chat", true);
        setSize(370, 360);
        setResizable(false);
        setLocationRelativeTo(parent);

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setBorder(new EmptyBorder(18, 18, 18, 18));
        setContentPane(content);

        // Imagen circular arriba y centrada
        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(90, 90));
        imageLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        try {
            originalImage = ImageIO.read(getClass().getResource("/chat.png"));
            setCircularImage(imageLabel, 90, 90);
        } catch (IOException | IllegalArgumentException e) {
            imageLabel.setText("Sin imagen");
        }
        content.add(imageLabel, BorderLayout.NORTH);

        // Redimensiona la imagen si cambia el tamaño del label
        imageLabel.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                if (originalImage != null) {
                    setCircularImage(imageLabel, imageLabel.getWidth(), imageLabel.getHeight());
                }
            }
        });

        // Panel de campos de entrada (IP, Puerto, Nombre)
        JPanel fields = new JPanel(new GridLayout(3, 2, 10, 14));
        fields.setOpaque(false);
        JLabel lblIP = new JLabel("IP:");
        JLabel lblPort = new JLabel("Puerto:");
        JLabel lblUser = new JLabel("Nombre:");

        Font labelFont = new Font("Segoe UI", Font.PLAIN, 15);
        lblIP.setFont(labelFont);
        lblPort.setFont(labelFont);
        lblUser.setFont(labelFont);

        txtIP = new JTextField("127.0.0.1");
        txtPort = new JTextField("9090");
        txtUsername = new JTextField("Usuario");

        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 15);
        txtIP.setFont(fieldFont);
        txtPort.setFont(fieldFont);
        txtUsername.setFont(fieldFont);

        txtIP.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(5, 8, 5, 8)
        ));
        txtPort.setBorder(txtIP.getBorder());
        txtUsername.setBorder(txtIP.getBorder());

        fields.add(lblIP);      fields.add(txtIP);
        fields.add(lblPort);    fields.add(txtPort);
        fields.add(lblUser);    fields.add(txtUsername);

        // Centra y limita el ancho de los campos
        JPanel fieldsWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        fieldsWrapper.setOpaque(false);
        fields.setPreferredSize(new Dimension(220, 110)); // Ajusta el tamaño según lo necesites
        fieldsWrapper.add(fields);

        content.add(fieldsWrapper, BorderLayout.CENTER);

        // Panel de botones (Conectar y Cancelar)
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton btnOk = new JButton("Conectar");
        JButton btnCancel = new JButton("Cancelar");

        btnOk.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnOk.setBackground(new Color(0, 120, 215));
        btnOk.setForeground(Color.WHITE);
        btnOk.setFocusPainted(false);

        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        btnCancel.setBackground(new Color(230, 230, 230));
        btnCancel.setFocusPainted(false);

        buttons.add(btnCancel);
        buttons.add(btnOk);
        content.add(buttons, BorderLayout.SOUTH);

        btnOk.addActionListener(e -> {
            confirmed = true;
            setVisible(false);
        });
        btnCancel.addActionListener(e -> {
            confirmed = false;
            setVisible(false);
        });

        getRootPane().setDefaultButton(btnOk);
    }

    // Recorta y escala la imagen en forma circular
    private void setCircularImage(JLabel label, int width, int height) {
        if (originalImage != null && width > 0 && height > 0) {
            int size = Math.min(width, height);
            BufferedImage scaled = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = scaled.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Recorta la imagen original en un círculo
            Ellipse2D.Double circle = new Ellipse2D.Double(0, 0, size, size);
            g2.setClip(circle);
            g2.drawImage(originalImage, 0, 0, size, size, null);
            g2.dispose();

            label.setIcon(new ImageIcon(scaled));
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getIP() {
        return txtIP.getText().trim();
    }

    public String getPort() {
        return txtPort.getText().trim();
    }

    public String getUsername() {
        return txtUsername.getText().trim();
    }
}