import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

import org.json.JSONArray;

public class SearchNewsApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            KeywordHistory.init();
            SavedWebManager.init();
            new LoginFrame();
        });
    }
}

/* ===================== LOGIN / REGISTER / FORGET PASSWORD ===================== */

class LoginFrame extends JFrame {
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JCheckBox rememberCheck;

    public LoginFrame() {
        setTitle("Đăng nhập - Tìm kiếm & so sánh sản phẩm");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 320);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Đăng nhập", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        panel.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 10));
        form.add(new JLabel("Tài khoản:"));
        usernameField = new JTextField();
        form.add(usernameField);

        form.add(new JLabel("Mật khẩu:"));
        passwordField = new JPasswordField();
        form.add(passwordField);

        rememberCheck = new JCheckBox("Ghi nhớ tài khoản");
        form.add(rememberCheck);
        form.add(new JLabel(""));
        panel.add(form, BorderLayout.CENTER);

        JButton loginBtn = new JButton("Đăng nhập");
        JButton registerBtn = new JButton("Đăng ký");
        JButton forgotBtn = new JButton("Quên mật ...");
        JButton clearBtn = new JButton("Xóa hết tài khoản máy này");

        loginBtn.addActionListener(e -> doLogin());
        registerBtn.addActionListener(e -> new RegisterFrame());
        forgotBtn.addActionListener(e -> new ForgetPasswordFrame());
        clearBtn.addActionListener(e -> clearAllAccounts());

        JPanel buttonRow = new JPanel(new GridLayout(1, 3, 10, 0));
        buttonRow.add(loginBtn);
        buttonRow.add(registerBtn);
        buttonRow.add(forgotBtn);

        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.add(buttonRow);
        bottom.add(Box.createVerticalStrut(8));

        clearBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        bottom.add(clearBtn);

        panel.add(bottom, BorderLayout.SOUTH);
        setContentPane(panel);

        String lastUser = UserManager.loadRememberedUser();
        if (lastUser != null) {
            usernameField.setText(lastUser);
            rememberCheck.setSelected(true);
        }

        setVisible(true);
    }

    private void doLogin() {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đủ thông tin");
            return;
        }

        if (UserManager.checkLogin(user, pass)) {
            if (rememberCheck.isSelected()) {
                UserManager.saveRememberedUser(user);
            } else {
                UserManager.clearRememberedUser();
            }
            dispose();
            new SearchFrame(user);
        } else {
            JOptionPane.showMessageDialog(this, "Sai tài khoản hoặc mật khẩu",
                    "Đăng nhập thất bại", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearAllAccounts() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Xóa TẤT CẢ tài khoản đã lưu trên máy này?\n"
                        + "(Xóa file .users.txt và .last_user.txt)\n"
                        + "Sau đó bạn phải tự Đăng ký lại.",
                "Xóa tài khoản",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            UserManager.deleteAllUsers();
            usernameField.setText("");
            passwordField.setText("");
            rememberCheck.setSelected(false);
            JOptionPane.showMessageDialog(this,
                    "Đã xóa toàn bộ tài khoản.\nBạn có thể Đăng ký tài khoản mới.",
                    "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}

class RegisterFrame extends JFrame {
    private final JTextField userField;
    private final JPasswordField passField;
    private final JPasswordField confirmField;

    public RegisterFrame() {
        setTitle("Đăng ký tài khoản mới");
        setSize(360, 250);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel main = new JPanel(new GridLayout(4, 2, 10, 10));
        main.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        main.add(new JLabel("Tên tài khoản:"));
        userField = new JTextField();
        main.add(userField);

        main.add(new JLabel("Mật khẩu:"));
        passField = new JPasswordField();
        main.add(passField);

        main.add(new JLabel("Nhập lại mật khẩu:"));
        confirmField = new JPasswordField();
        main.add(confirmField);

        JButton registerBtn = new JButton("Đăng ký");
        JButton cancelBtn = new JButton("Hủy");
        registerBtn.addActionListener(e -> register());
        cancelBtn.addActionListener(e -> dispose());
        main.add(registerBtn);
        main.add(cancelBtn);

        setContentPane(main);
        setVisible(true);
    }

    private void register() {
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword());
        String confirm = new String(confirmField.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không được bỏ trống!");
            return;
        }
        if (!pass.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu nhập lại không khớp!");
            return;
        }
        if (UserManager.userExists(user)) {
            JOptionPane.showMessageDialog(this, "Tài khoản đã tồn tại!");
            return;
        }

        UserManager.addUser(user, pass);
        JOptionPane.showMessageDialog(this, "Đăng ký thành công!");
        dispose();
    }
}

class ForgetPasswordFrame extends JFrame {
    private final JTextField userField;

    public ForgetPasswordFrame() {
        setTitle("Quên mật khẩu");
        setSize(320, 180);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel main = new JPanel(new GridLayout(3, 1, 10, 10));
        main.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        main.add(new JLabel("Nhập tên tài khoản của bạn:"));
        userField = new JTextField();
        main.add(userField);

        JButton resetBtn = new JButton("Lấy lại mật khẩu");
        resetBtn.addActionListener(e -> recover());
        main.add(resetBtn);

        setContentPane(main);
        setVisible(true);
    }

    private void recover() {
        String user = userField.getText().trim();
        if (user.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nhập tên tài khoản!");
            return;
        }

        String pwd = UserManager.getPassword(user);
        if (pwd == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy tài khoản!");
        } else {
            JOptionPane.showMessageDialog(this, "Mật khẩu của bạn là: " + pwd,
                    "Khôi phục mật khẩu", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}

/* ===================== USER MANAGER ===================== */

class UserManager {
    private static final Path FILE =
            Paths.get(System.getProperty("user.home", "."), ".users.txt");
    private static final Path REMEMBER_FILE =
            Paths.get(System.getProperty("user.home", "."), ".last_user.txt");
    private static final Map<String, String> USERS = new HashMap<>();

    static {
        loadUsers();
    }

    private static void loadUsers() {
        USERS.clear();
        if (!Files.exists(FILE)) return;
        try (BufferedReader br = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) USERS.put(parts[0], parts[1]);
            }
        } catch (IOException ignored) {
        }
    }

    private static void save() {
        try (BufferedWriter bw = Files.newBufferedWriter(FILE, StandardCharsets.UTF_8)) {
            for (Map.Entry<String, String> e : USERS.entrySet()) {
                bw.write(e.getKey() + ":" + e.getValue());
                bw.newLine();
            }
        } catch (IOException ignored) {
        }
    }

    public static void addUser(String user, String pass) {
        USERS.put(user, pass);
        save();
    }

    public static boolean checkLogin(String user, String pass) {
        return USERS.containsKey(user) && USERS.get(user).equals(pass);
    }

    public static boolean userExists(String user) {
        return USERS.containsKey(user);
    }

    public static String getPassword(String user) {
        return USERS.get(user);
    }

    public static void deleteUser(String user) {
        if (user == null) return;
        if (USERS.remove(user) != null) save();
    }

    public static void deleteAllUsers() {
        USERS.clear();
        save();
        clearRememberedUser();
        try {
            Files.deleteIfExists(FILE);
        } catch (IOException ignored) {
        }
    }

    public static void saveRememberedUser(String user) {
        try {
            Files.writeString(REMEMBER_FILE, user, StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }

    public static String loadRememberedUser() {
        if (!Files.exists(REMEMBER_FILE)) return null;
        try {
            return Files.readString(REMEMBER_FILE, StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            return null;
        }
    }

    public static void clearRememberedUser() {
        try {
            Files.deleteIfExists(REMEMBER_FILE);
        } catch (IOException ignored) {
        }
    }
}

/* ===================== SEARCH RESULT MODEL ===================== */

class SearchResult {
    public final String site;
    public final String title;
    public final String url;
    public final long price;

    public SearchResult(String site, String title, String url, long price) {
        this.site = site;
        this.title = title;
        this.url = url;
        this.price = price;
    }

    private String formatPrice(long v) {
        String s = String.format("%,d", v);
        s = s.replace(',', '.');
        return s + " đ";
    }

    public String formattedPrice() {
        if (price <= 0) return "";
        return formatPrice(price);
    }
}

/* ===================== SEARCH SERVICE (KHÔNG DÙNG API, LINK THẬT) ===================== */

class SearchService {

    private static class Seller {
        final String name;
        final String domain;
        final String urlTemplate;

        Seller(String name, String domain, String urlTemplate) {
            this.name = name;
            this.domain = domain;
            this.urlTemplate = urlTemplate;
        }
    }

    private static final Seller[] SELLERS = {
            new Seller("Shopee",           "shopee.vn",
                    "https://shopee.vn/search?keyword=%s"),
            new Seller("Lazada",           "lazada.vn",
                    "https://www.lazada.vn/catalog/?q=%s"),
            new Seller("Tiki",             "tiki.vn",
                    "https://tiki.vn/search?q=%s"),
            new Seller("Sendo",            "sendo.vn",
                    "https://www.sendo.vn/tim-kiem?q=%s"),
            new Seller("Thế Giới Di Động", "thegioididong.com",
                    "https://www.thegioididong.com/tim-kiem?key=%s"),
            new Seller("Điện máy XANH",    "dienmayxanh.com",
                    "https://www.dienmayxanh.com/tim-kiem?key=%s"),
            new Seller("FPT Shop",         "fptshop.com.vn",
                    "https://fptshop.com.vn/tim-kiem/%s"),
            new Seller("CellphoneS",       "cellphones.com.vn",
                    "https://cellphones.com.vn/search?query=%s"),
            new Seller("Hoàng Hà Mobile",  "hoanghamobile.com",
                    "https://hoanghamobile.com/tim-kiem?q=%s"),
            new Seller("Phong Vũ",         "phongvu.vn",
                    "https://phongvu.vn/search?q=%s"),
            new Seller("Nguyễn Kim",       "nguyenkim.com",
                    "https://www.nguyenkim.com/search?q=%s"),
            new Seller("An Phát",          "anphatpc.com.vn",
                    "https://anphatpc.com.vn/search?keyword=%s"),
            new Seller("GearVN",           "gearvn.com",
                    "https://gearvn.com/search?type=product&q=%s"),
            new Seller("Hanoicomputer",    "hanoicomputer.vn",
                    "https://hanoicomputer.vn/search?q=%s"),
            new Seller("Meta.vn",          "meta.vn",
                    "https://meta.vn/tim-kiem/?q=%s")
    };

    private static final Map<String, String> DOMAIN_TO_NAME = new LinkedHashMap<>();
    private static final Set<String> SELLER_NAME_SET = new LinkedHashSet<>();

    static {
        for (Seller s : SELLERS) {
            SELLER_NAME_SET.add(s.name);
            DOMAIN_TO_NAME.put(s.domain, s.name);
        }
    }

    public static boolean isSellerSite(String siteName) {
        return SELLER_NAME_SET.contains(siteName);
    }

    public static java.util.List<SearchResult> searchProduct(String keyword) {
        java.util.List<SearchResult> results = new ArrayList<>();
        if (keyword == null) return results;
        String kw = keyword.trim();
        if (kw.isEmpty()) return results;

        String kwUrl = URLEncoder.encode(kw, StandardCharsets.UTF_8);

        for (Seller s : SELLERS) {
            String url = String.format(s.urlTemplate, kwUrl);
            String title = "Tìm \"" + kw + "\" trên " + s.name;
            long price = simulatePrice(kw, s.name, url);
            results.add(new SearchResult(s.name, title, url, price));
        }
        return results;
    }

    private static long simulatePrice(String keyword, String siteName, String url) {
        long base = 500_000L + Math.abs(keyword.hashCode() % 4_000_000L);
        long noise1 = Math.abs(siteName.hashCode() % 2_000_000L);
        long noise2 = Math.abs(url.hashCode() % 1_500_000L);
        return base + noise1 / 2 + noise2 / 3;
    }
}

/* ===================== LƯU WEB ===================== */

class SavedWebManager {
    private static Path file;
    private static final java.util.List<SearchResult> saved = new ArrayList<>();

    public static void init() {
        if (file != null) return;
        String home = System.getProperty("user.home", ".");
        file = Paths.get(home, ".saved_webs.txt");
        load();
    }

    private static void load() {
        saved.clear();
        if (!Files.exists(file)) return;
        try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|", 4);
                if (parts.length == 4) {
                    String site = parts[0];
                    String title = parts[1];
                    String url = parts[2];
                    long price = Long.parseLong(parts[3]);
                    saved.add(new SearchResult(site, title, url, price));
                }
            }
        } catch (IOException | NumberFormatException ignored) {
        }
    }

    private static void save() {
        if (file == null) return;
        try (BufferedWriter bw = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            for (SearchResult r : saved) {
                bw.write(r.site + "|" + r.title + "|" + r.url + "|" + r.price);
                bw.newLine();
            }
        } catch (IOException ignored) {
        }
    }

    public static void add(SearchResult r) {
        saved.add(r);
        save();
    }

    public static void remove(SearchResult r) {
        if (r == null) return;
        Iterator<SearchResult> it = saved.iterator();
        while (it.hasNext()) {
            SearchResult s = it.next();
            if (s.site.equals(r.site)
                    && s.title.equals(r.title)
                    && s.url.equals(r.url)
                    && s.price == r.price) {
                it.remove();
                save();
                break;
            }
        }
    }

    public static java.util.List<SearchResult> getAll() {
        return new ArrayList<>(saved);
    }
}

/* ===================== MÀN HÌNH CHÍNH ===================== */

class SearchFrame extends JFrame {
    private final String currentUser;

    private final JTextField productField;
    private final DefaultListModel<String> siteListModel;
    private final JList<String> siteList;
    private final DefaultListModel<String> resultListModel;
    private final JList<String> resultList;
    private final JPopupMenu resultPopup;

    private final JPopupMenu suggestPopup;
    private final JList<String> suggestList;

    private final java.util.List<SearchResult> allResults = new ArrayList<>();
    private final java.util.List<SearchResult> displayedResults = new ArrayList<>();

    private static final int PAGE_SIZE = 20;
    private int currentPage = 1;
    private int totalPages = 1;
    private java.util.List<SearchResult> currentSourceList = new ArrayList<>();
    private JButton prevPageBtn;
    private JButton nextPageBtn;
    private JLabel pageInfoLabel;

    public SearchFrame(String username) {
        this.currentUser = username;

        setTitle("Tìm kiếm & so sánh sản phẩm");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(980, 620));
        setLocationRelativeTo(null);

        JMenuBar menuBar = new JMenuBar();
        JMenu accountMenu = new JMenu("Tài khoản");
        JMenuItem deleteAccountItem = new JMenuItem("Xóa tài khoản hiện tại");
        JMenuItem logoutItem = new JMenuItem("Đăng xuất");
        deleteAccountItem.addActionListener(e -> deleteCurrentAccount());
        logoutItem.addActionListener(e -> doLogout());
        accountMenu.add(deleteAccountItem);
        accountMenu.addSeparator();
        accountMenu.add(logoutItem);
        menuBar.add(accountMenu);

        JLabel userLabel = new JLabel("Xin chào, " + username + "  ");
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(userLabel);
        setJMenuBar(menuBar);

        JLabel titleLabel = new JLabel("Tìm kiếm & so sánh giá sản phẩm", SwingConstants.CENTER);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 24f));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // LEFT
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        JLabel webLabel = new JLabel("Các web bán sản phẩm (lọc theo web)");
        leftPanel.add(webLabel, BorderLayout.NORTH);
        siteListModel = new DefaultListModel<>();
        siteList = new JList<>(siteListModel);
        siteList.setFixedCellHeight(24);
        JScrollPane siteScroll = new JScrollPane(siteList);
        leftPanel.add(siteScroll, BorderLayout.CENTER);
        leftPanel.setPreferredSize(new Dimension(230, 0));
        centerPanel.add(leftPanel, BorderLayout.WEST);

        // RIGHT
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        JLabel resultLabel = new JLabel("Kết quả (double-click mở web / chuột phải để lưu)");
        rightPanel.add(resultLabel, BorderLayout.NORTH);
        resultListModel = new DefaultListModel<>();
        resultList = new JList<>(resultListModel);
        resultList.setFixedCellHeight(32);
        JScrollPane resultScroll = new JScrollPane(resultList);
        rightPanel.add(resultScroll, BorderLayout.CENTER);
        rightPanel.setPreferredSize(new Dimension(520, 0));

        JPanel pagePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        prevPageBtn = new JButton("Trang trước");
        nextPageBtn = new JButton("Trang sau");
        pageInfoLabel = new JLabel("Trang: 0 / 0 (0 kết quả)");
        prevPageBtn.addActionListener(e -> goToPage(currentPage - 1));
        nextPageBtn.addActionListener(e -> goToPage(currentPage + 1));
        pagePanel.add(prevPageBtn);
        pagePanel.add(pageInfoLabel);
        pagePanel.add(nextPageBtn);
        rightPanel.add(pagePanel, BorderLayout.SOUTH);

        centerPanel.add(rightPanel, BorderLayout.EAST);

        // Context menu lưu web
        resultPopup = new JPopupMenu();
        JMenuItem saveItem = new JMenuItem("Lưu web này");
        saveItem.addActionListener(e -> saveSelectedWeb());
        resultPopup.add(saveItem);

        // MIDDLE
        JPanel middlePanel = new JPanel();
        middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.Y_AXIS));
        middlePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel keywordLabel = new JLabel("Nhập từ khóa / tên sản phẩm (gõ được tiếng Việt)");
        keywordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel searchRow = new JPanel();
        searchRow.setLayout(new BoxLayout(searchRow, BoxLayout.X_AXIS));
        searchRow.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        productField = new JTextField(18);
        productField.setMaximumSize(productField.getPreferredSize());
        JButton searchBtn = new JButton("Tìm kiếm");
        searchRow.add(productField);
        searchRow.add(Box.createHorizontalStrut(10));
        searchRow.add(searchBtn);
        searchRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        middlePanel.add(keywordLabel);
        middlePanel.add(Box.createVerticalStrut(8));
        middlePanel.add(searchRow);
        middlePanel.add(Box.createVerticalStrut(30));

        JButton compareBtn = new JButton("So sánh sản phẩm");
        compareBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        compareBtn.setPreferredSize(new Dimension(180, 35));
        compareBtn.setMaximumSize(new Dimension(180, 35));
        middlePanel.add(compareBtn);
        middlePanel.add(Box.createVerticalStrut(15));

        JButton savedListBtn = new JButton("Các web đã lưu");
        savedListBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        savedListBtn.setPreferredSize(new Dimension(180, 35));
        savedListBtn.setMaximumSize(new Dimension(180, 35));
        middlePanel.add(savedListBtn);

        // NÚT QUAY LẠI ĐĂNG NHẬP
        middlePanel.add(Box.createVerticalStrut(15));
        JButton backLoginBtn = new JButton("Quay lại đăng nhập");
        backLoginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        backLoginBtn.setPreferredSize(new Dimension(180, 35));
        backLoginBtn.setMaximumSize(new Dimension(180, 35));
        backLoginBtn.addActionListener(e -> doLogout());
        middlePanel.add(backLoginBtn);

        JPanel middleWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 40));
        middleWrapper.add(middlePanel);
        centerPanel.add(middleWrapper, BorderLayout.CENTER);

        // GỢI Ý TỪ KHÓA
        suggestPopup = new JPopupMenu();
        suggestList = new JList<>();
        suggestList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane suggestScroll = new JScrollPane(suggestList);
        suggestScroll.setBorder(null);
        suggestPopup.add(suggestScroll);
        suggestPopup.setFocusable(false);

        productField.addActionListener(e -> performSearch());
        searchBtn.addActionListener(e -> performSearch());

        productField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { updateSuggestions(); }
            @Override
            public void removeUpdate(DocumentEvent e) { updateSuggestions(); }
            @Override
            public void changedUpdate(DocumentEvent e) { updateSuggestions(); }
        });

        InputMap im = productField.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap am = productField.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "moveToSuggest");
        am.put("moveToSuggest", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (suggestPopup.isVisible() && suggestList.getModel().getSize() > 0) {
                    suggestList.requestFocusInWindow();
                    suggestList.setSelectedIndex(0);
                }
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "hideSuggest");
        am.put("hideSuggest", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                suggestPopup.setVisible(false);
            }
        });

        suggestList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) chooseSuggestion();
            }
        });

        suggestList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    chooseSuggestion();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    suggestPopup.setVisible(false);
                    productField.requestFocusInWindow();
                } else if (e.getKeyCode() == KeyEvent.VK_UP &&
                        suggestList.getSelectedIndex() == 0) {
                    suggestPopup.setVisible(false);
                    productField.requestFocusInWindow();
                }
            }
        });

        siteList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) applySiteFilter();
        });

        resultList.addMouseListener(new MouseAdapter() {
            private void showPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int index = resultList.locationToIndex(e.getPoint());
                    if (index >= 0 && index < displayedResults.size()) {
                        resultList.setSelectedIndex(index);
                        resultPopup.show(resultList, e.getX(), e.getY());
                    }
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
                    int index = resultList.locationToIndex(e.getPoint());
                    if (index >= 0 && index < displayedResults.size()) {
                        SearchResult r = displayedResults.get(index);
                        openInBrowser(r.url);
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e)  { showPopup(e); }
            @Override
            public void mouseReleased(MouseEvent e) { showPopup(e); }
        });

        compareBtn.addActionListener(e -> openCompareDialog());
        savedListBtn.addActionListener(e -> openBookmarksDialog());

        setContentPane(mainPanel);
        pack();
        setVisible(true);

        refreshResultPage();
    }

    private void updateSuggestions() {
        String text = productField.getText().trim();
        if (text.isEmpty()) {
            suggestPopup.setVisible(false);
            return;
        }

        java.util.List<String> suggs = KeywordHistory.getSuggestions(text);
        if (suggs.isEmpty()) {
            suggestPopup.setVisible(false);
            return;
        }

        DefaultListModel<String> model = new DefaultListModel<>();
        for (String s : suggs) model.addElement(s);
        suggestList.setModel(model);
        suggestList.setVisibleRowCount(Math.min(8, model.size()));

        int width = productField.getWidth() + 20;
        int height = suggestList.getPreferredScrollableViewportSize().height;
        suggestPopup.setPopupSize(width, height);
        suggestPopup.show(productField, -10, productField.getHeight());

        SwingUtilities.invokeLater(() -> productField.requestFocusInWindow());
    }

    private void chooseSuggestion() {
        String sel = suggestList.getSelectedValue();
        if (sel != null) {
            productField.setText(sel);
            suggestPopup.setVisible(false);
            productField.requestFocusInWindow();
            productField.setCaretPosition(sel.length());
        }
    }

    private void performSearch() {
        String keyword = productField.getText().trim();
        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Hãy nhập từ khóa / tên sản phẩm",
                    "Thiếu từ khóa", JOptionPane.WARNING_MESSAGE);
            return;
        }

        KeywordHistory.addKeyword(keyword);

        allResults.clear();
        allResults.addAll(SearchService.searchProduct(keyword));

        siteListModel.clear();
        Set<String> sellerSites = new LinkedHashSet<>();
        for (SearchResult r : allResults) {
            if (SearchService.isSellerSite(r.site)) {
                sellerSites.add(r.site);
            }
        }
        for (String s : sellerSites) siteListModel.addElement(s);

        updateResultList(allResults, false);
    }

    private java.util.List<SearchResult> getBaseListByCurrentSite() {
        String site = siteList.getSelectedValue();
        if (site == null) return new ArrayList<>(allResults);
        java.util.List<SearchResult> filtered = new ArrayList<>();
        for (SearchResult r : allResults) {
            if (site.equals(r.site)) filtered.add(r);
        }
        return filtered;
    }

    private void applySiteFilter() {
        java.util.List<SearchResult> base = getBaseListByCurrentSite();
        updateResultList(base, true);
    }

    private void updateResultList(java.util.List<SearchResult> source, boolean sortByPrice) {
        currentSourceList = new ArrayList<>();
        if (sortByPrice) {
            for (SearchResult r : source) {
                if (r.price > 0) currentSourceList.add(r);
            }
            currentSourceList.sort(Comparator.comparingLong(r -> r.price));
        } else {
            currentSourceList.addAll(source);
        }
        currentPage = 1;
        refreshResultPage();
    }

    private void refreshResultPage() {
        resultListModel.clear();
        displayedResults.clear();

        if (currentSourceList == null || currentSourceList.isEmpty()) {
            pageInfoLabel.setText("Trang: 0 / 0 (0 kết quả)");
            prevPageBtn.setEnabled(false);
            nextPageBtn.setEnabled(false);
            return;
        }

        int total = currentSourceList.size();
        totalPages = (total + PAGE_SIZE - 1) / PAGE_SIZE;
        if (currentPage < 1) currentPage = 1;
        if (currentPage > totalPages) currentPage = totalPages;

        int start = (currentPage - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, total);

        for (int i = start; i < end; i++) {
            SearchResult r = currentSourceList.get(i);
            String shortUrl = r.url;
            if (shortUrl.length() > 80) shortUrl = shortUrl.substring(0, 77) + "...";

            String display = r.title + "  [" + r.site + "]  -  " + shortUrl;
            resultListModel.addElement(display);
            displayedResults.add(r);
        }

        pageInfoLabel.setText("Trang: " + currentPage + " / " + totalPages + "  (" + total + " kết quả)");
        prevPageBtn.setEnabled(currentPage > 1);
        nextPageBtn.setEnabled(currentPage < totalPages);
    }

    private void goToPage(int page) {
        if (currentSourceList == null || currentSourceList.isEmpty()) return;
        int maxPage = (currentSourceList.size() + PAGE_SIZE - 1) / PAGE_SIZE;
        if (page < 1 || page > maxPage) return;
        currentPage = page;
        refreshResultPage();
    }

    private void saveSelectedWeb() {
        int index = resultList.getSelectedIndex();
        if (index < 0 || index >= displayedResults.size()) {
            JOptionPane.showMessageDialog(this,
                    "Hãy chọn một dòng trong phần kết quả trước khi lưu.",
                    "Chưa chọn web", JOptionPane.WARNING_MESSAGE);
            return;
        }
        SearchResult r = displayedResults.get(index);
        SavedWebManager.add(r);
        JOptionPane.showMessageDialog(this,
                "Đã lưu: " + r.site,
                "Lưu web", JOptionPane.INFORMATION_MESSAGE);
    }

    private void openBookmarksDialog() {
        java.util.List<SearchResult> saved = SavedWebManager.getAll();
        if (saved.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Chưa lưu web nào.",
                    "Web đã lưu", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        BookmarksDialog dialog = new BookmarksDialog(this, saved);
        dialog.setVisible(true);
    }

    private void openInBrowser(String url) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Không mở được trình duyệt:\n" + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Hệ thống không hỗ trợ mở trình duyệt.",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openCompareDialog() {
        java.util.List<SearchResult> sellerResults = new ArrayList<>();
        for (SearchResult r : allResults) {
            if (SearchService.isSellerSite(r.site) && r.price > 0) {
                sellerResults.add(r);
            }
        }

        if (sellerResults.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Chưa có dữ liệu sản phẩm để so sánh.\nHãy nhập từ khóa và nhấn Tìm kiếm trước.",
                    "Chưa có dữ liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }

        CompareDialog dialog = new CompareDialog(this, sellerResults);
        dialog.setVisible(true);
    }

    private void doLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn đăng xuất?", "Đăng xuất",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            new LoginFrame();
            dispose();
        }
    }

    private void deleteCurrentAccount() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa tài khoản '" + currentUser + "'?\n"
                        + "Sau khi xóa, người khác có thể tự đăng ký tài khoản mới.",
                "Xóa tài khoản",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            UserManager.deleteUser(currentUser);
            UserManager.clearRememberedUser();
            JOptionPane.showMessageDialog(this,
                    "Đã xóa tài khoản. Bạn có thể đăng ký lại tài khoản mới.",
                    "Xóa tài khoản", JOptionPane.INFORMATION_MESSAGE);
            new LoginFrame();
            dispose();
        }
    }
}

/* ===================== BOOKMARKS DIALOG ===================== */

class BookmarksDialog extends JDialog {
    private final java.util.List<SearchResult> saved;
    private final DefaultListModel<String> listModel;
    private final JList<String> list;

    public BookmarksDialog(JFrame owner, java.util.List<SearchResult> saved) {
        super(owner, "Các web đã lưu", true);
        this.saved = new ArrayList<>(saved);

        setSize(650, 450);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("Danh sách web đã lưu (double-click để mở)", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        add(title, BorderLayout.NORTH);

        listModel = new DefaultListModel<>();
        list = new JList<>(listModel);
        list.setFixedCellHeight(30);
        JScrollPane scroll = new JScrollPane(list);
        add(scroll, BorderLayout.CENTER);

        JButton deleteBtn = new JButton("Xóa web đã chọn");
        JButton closeBtn = new JButton("Đóng");

        deleteBtn.addActionListener(e -> deleteSelected());
        closeBtn.addActionListener(e -> dispose());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(deleteBtn);
        bottom.add(closeBtn);
        add(bottom, BorderLayout.SOUTH);

        fillData();

        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = list.locationToIndex(e.getPoint());
                    if (index >= 0 && index < BookmarksDialog.this.saved.size()) {
                        SearchResult r = BookmarksDialog.this.saved.get(index);
                        openInBrowser(r.url);
                    }
                }
            }
        });
    }

    private void fillData() {
        listModel.clear();
        for (SearchResult r : saved) {
            String shortUrl = r.url;
            if (shortUrl.length() > 70) shortUrl = shortUrl.substring(0, 67) + "...";
            String pricePart = "";
            if (SearchService.isSellerSite(r.site) && r.price > 0) {
                pricePart = " - " + r.formattedPrice();
            }
            listModel.addElement(r.site + " - " + r.title + pricePart + " - " + shortUrl);
        }
    }

    private void deleteSelected() {
        int index = list.getSelectedIndex();
        if (index < 0 || index >= saved.size()) {
            JOptionPane.showMessageDialog(this,
                    "Hãy chọn một dòng để xóa.",
                    "Chưa chọn web", JOptionPane.WARNING_MESSAGE);
            return;
        }
        SearchResult r = saved.get(index);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa web này khỏi danh sách lưu?",
                "Xóa web đã lưu",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            saved.remove(index);
            SavedWebManager.remove(r);
            fillData();
        }
    }

    private void openInBrowser(String url) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Không mở được trình duyệt:\n" + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Hệ thống không hỗ trợ mở trình duyệt.",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}

/* ===================== SO SÁNH GIÁ ===================== */

class CompareDialog extends JDialog {
    private static class PriceOption {
        final String label;
        final Long value;
        PriceOption(String label, Long value) { this.label = label; this.value = value; }
        @Override public String toString() { return label; }
    }

    private final java.util.List<SearchResult> allResults;
    private final java.util.List<SearchResult> filteredResults = new ArrayList<>();

    private final JComboBox<PriceOption> minPriceCombo;
    private final JComboBox<PriceOption> maxPriceCombo;
    private final DefaultListModel<String> filterListModel;
    private final JList<String> filterList;
    private final JComboBox<String> site1Combo;
    private final JComboBox<String> site2Combo;
    private final JLabel compareResultLabel;

    public CompareDialog(JFrame owner, java.util.List<SearchResult> results) {
        super(owner, "So sánh sản phẩm", true);
        this.allResults = new ArrayList<>(results);

        setSize(780, 470);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("So sánh sản phẩm theo giá", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(1, 2, 10, 10));
        add(center, BorderLayout.CENTER);

        // Left panel: lọc theo khoảng giá
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Danh sách sản phẩm sau khi lọc"));

        JPanel topFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 3));
        topFilter.add(new JLabel("Giá từ:"));

        minPriceCombo = new JComboBox<>();
        minPriceCombo.addItem(new PriceOption("Không giới hạn", null));
        minPriceCombo.addItem(new PriceOption("0 đ", 0L));
        minPriceCombo.addItem(new PriceOption("500.000 đ", 500_000L));
        minPriceCombo.addItem(new PriceOption("1.000.000 đ", 1_000_000L));
        minPriceCombo.addItem(new PriceOption("2.000.000 đ", 2_000_000L));
        minPriceCombo.addItem(new PriceOption("3.000.000 đ", 3_000_000L));
        minPriceCombo.addItem(new PriceOption("5.000.000 đ", 5_000_000L));
        minPriceCombo.addItem(new PriceOption("10.000.000 đ", 10_000_000L));
        topFilter.add(minPriceCombo);

        topFilter.add(new JLabel("đ đến:"));

        maxPriceCombo = new JComboBox<>();
        maxPriceCombo.addItem(new PriceOption("Không giới hạn", null));
        maxPriceCombo.addItem(new PriceOption("1.000.000 đ", 1_000_000L));
        maxPriceCombo.addItem(new PriceOption("2.000.000 đ", 2_000_000L));
        maxPriceCombo.addItem(new PriceOption("3.000.000 đ", 3_000_000L));
        maxPriceCombo.addItem(new PriceOption("5.000.000 đ", 5_000_000L));
        maxPriceCombo.addItem(new PriceOption("10.000.000 đ", 10_000_000L));
        maxPriceCombo.addItem(new PriceOption("20.000.000 đ", 20_000_000L));
        maxPriceCombo.addItem(new PriceOption("50.000.000 đ", 50_000_000L));
        topFilter.add(maxPriceCombo);

        JButton filterBtn = new JButton("Lọc");
        topFilter.add(filterBtn);

        JButton minBtn = new JButton("Giá thấp nhất");
        JButton maxBtn = new JButton("Giá cao nhất");
        JPanel minMaxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 3));
        minMaxPanel.add(minBtn);
        minMaxPanel.add(maxBtn);

        JPanel topLeft = new JPanel();
        topLeft.setLayout(new BoxLayout(topLeft, BoxLayout.Y_AXIS));
        topLeft.add(topFilter);
        topLeft.add(minMaxPanel);

        leftPanel.add(topLeft, BorderLayout.NORTH);

        filterListModel = new DefaultListModel<>();
        filterList = new JList<>(filterListModel);
        filterList.setFixedCellHeight(28);
        JScrollPane filterScroll = new JScrollPane(filterList);
        leftPanel.add(filterScroll, BorderLayout.CENTER);

        center.add(leftPanel);

        // Right panel: chọn 2 web để so sánh
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(BorderFactory.createTitledBorder("So sánh giữa 2 web"));

        rightPanel.add(Box.createVerticalStrut(5));
        rightPanel.add(new JLabel("Chọn 2 web để so sánh:"));
        rightPanel.add(Box.createVerticalStrut(10));

        site1Combo = new JComboBox<>();
        site2Combo = new JComboBox<>();
        Set<String> sites = new LinkedHashSet<>();
        for (SearchResult r : allResults) sites.add(r.site);
        for (String s : sites) {
            site1Combo.addItem(s);
            site2Combo.addItem(s);
        }

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row1.add(new JLabel("Web 1:"));
        row1.add(site1Combo);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row2.add(new JLabel("Web 2:"));
        row2.add(site2Combo);

        JButton compareBtn = new JButton("So sánh 2 web");

        compareResultLabel = new JLabel(" ");
        compareResultLabel.setVerticalAlignment(SwingConstants.TOP);

        rightPanel.add(row1);
        rightPanel.add(row2);
        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(compareBtn);
        rightPanel.add(Box.createVerticalStrut(15));
        rightPanel.add(compareResultLabel);

        center.add(rightPanel);

        JButton closeBtn = new JButton("Đóng");
        closeBtn.addActionListener(e -> dispose());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(closeBtn);
        add(bottom, BorderLayout.SOUTH);

        updateFilterList(allResults);

        filterBtn.addActionListener(e -> applyRangeFilter());
        minPriceCombo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) applyRangeFilter();
        });
        maxPriceCombo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) applyRangeFilter();
        });

        minBtn.addActionListener(e -> showExtremePrice(true));
        maxBtn.addActionListener(e -> showExtremePrice(false));
        compareBtn.addActionListener(e -> compareTwoSites());

        filterList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = filterList.locationToIndex(e.getPoint());
                    if (index >= 0 && index < filteredResults.size()) {
                        SearchResult r = filteredResults.get(index);
                        openInBrowser(r.url);
                    }
                }
            }
        });
    }

    private void updateFilterList(java.util.List<SearchResult> source) {
        filterListModel.clear();
        filteredResults.clear();
        for (SearchResult r : source) {
            filterListModel.addElement(r.site + " - " + r.title + " - " + r.formattedPrice());
            filteredResults.add(r);
        }
    }

    private void applyRangeFilter() {
        PriceOption minOpt = (PriceOption) minPriceCombo.getSelectedItem();
        PriceOption maxOpt = (PriceOption) maxPriceCombo.getSelectedItem();
        Long min = minOpt == null ? null : minOpt.value;
        Long max = maxOpt == null ? null : maxOpt.value;

        if (min != null && max != null && min > max) {
            JOptionPane.showMessageDialog(this,
                    "Giá tối thiểu không được lớn hơn giá tối đa",
                    "Lỗi khoảng giá", JOptionPane.WARNING_MESSAGE);
            return;
        }

        java.util.List<SearchResult> filtered = new ArrayList<>();
        for (SearchResult r : allResults) {
            if (min != null && r.price < min) continue;
            if (max != null && r.price > max) continue;
            filtered.add(r);
        }
        updateFilterList(filtered);
    }

    private void showExtremePrice(boolean isMin) {
        if (allResults.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chưa có dữ liệu để so sánh");
            return;
        }
        SearchResult best = allResults.get(0);
        for (SearchResult r : allResults) {
            if (isMin) {
                if (r.price < best.price) best = r;
            } else {
                if (r.price > best.price) best = r;
            }
        }
        String msg = (isMin ? "Giá thấp nhất: " : "Giá cao nhất: ")
                + best.site + " - " + best.title + " - " + best.formattedPrice();
        JOptionPane.showMessageDialog(this, msg, "Kết quả", JOptionPane.INFORMATION_MESSAGE);
    }

    private void compareTwoSites() {
        String site1 = (String) site1Combo.getSelectedItem();
        String site2 = (String) site2Combo.getSelectedItem();

        if (site1 == null || site2 == null) {
            JOptionPane.showMessageDialog(this,
                    "Cần chọn đủ 2 web để so sánh",
                    "Thiếu lựa chọn", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (site1.equals(site2)) {
            JOptionPane.showMessageDialog(this,
                    "Hãy chọn 2 web khác nhau để so sánh.",
                    "Trùng web", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SearchResult r1 = getCheapestForSite(site1);
        SearchResult r2 = getCheapestForSite(site2);

        if (r1 == null || r2 == null) {
            JOptionPane.showMessageDialog(this,
                    "Không tìm thấy dữ liệu giá cho 1 trong 2 web.",
                    "Thiếu dữ liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append(site1).append(": ").append(r1.formattedPrice()).append("<br>");
        sb.append(site2).append(": ").append(r2.formattedPrice()).append("<br><br>");

        if (r1.price == r2.price) {
            sb.append("Giá 2 web bằng nhau.");
        } else {
            SearchResult cheaper = r1.price < r2.price ? r1 : r2;
            SearchResult expensive = r1.price < r2.price ? r2 : r1;
            long diff = expensive.price - cheaper.price;
            String diffStr = String.format("%,d", diff).replace(',', '.') + " đ";
            sb.append("Web rẻ hơn: ").append(cheaper.site)
                    .append(" (rẻ hơn ").append(diffStr)
                    .append(" so với ").append(expensive.site).append(")");
        }
        sb.append("</html>");

        compareResultLabel.setText(sb.toString());
    }

    private SearchResult getCheapestForSite(String site) {
        SearchResult best = null;
        for (SearchResult r : allResults) {
            if (!r.site.equals(site)) continue;
            if (best == null || r.price < best.price) best = r;
        }
        return best;
    }

    private void openInBrowser(String url) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Không mở được trình duyệt:\n" + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Hệ thống không hỗ trợ mở trình duyệt.",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}

/* ===================== GỢI Ý TỪ KHÓA - GOOGLE SUGGEST ===================== */

class SuggestService {
    public static java.util.List<String> fetchGoogleSuggestions(String query) {
        java.util.List<String> result = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) return result;

        try {
            String qEncoded = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8);
            String urlStr = "https://suggestqueries.google.com/complete/search"
                    + "?client=firefox"
                    + "&hl=vi"
                    + "&gl=vn"
                    + "&q=" + qEncoded;

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                            + "(KHTML, like Gecko) Chrome/120.0 Safari/537.36");

            int code = conn.getResponseCode();
            if (code != 200) {
                conn.disconnect();
                return result;
            }

            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            }
            conn.disconnect();

            JSONArray root = new JSONArray(sb.toString());
            JSONArray arr = root.getJSONArray(1);
            for (int i = 0; i < arr.length(); i++) {
                String s = arr.getString(i);
                if (!s.isBlank()) result.add(s);
            }

        } catch (Exception e) {
            // lỗi thì bỏ qua, chỉ dùng lịch sử local
        }
        return result;
    }
}

/* ===================== LỊCH SỬ TỪ KHÓA ===================== */

class KeywordHistory {
    private static final Set<String> keywords = new LinkedHashSet<>();
    private static Path historyFile;

    public static void init() {
        String userHome = System.getProperty("user.home", ".");
        historyFile = Paths.get(userHome, ".search_keywords.txt");
        load();
    }

    private static void load() {
        if (historyFile == null || !Files.exists(historyFile)) return;
        try (BufferedReader reader =
                     Files.newBufferedReader(historyFile, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) keywords.add(line);
            }
        } catch (IOException e) {
            System.err.println("Không đọc được file lịch sử từ khóa: " + e.getMessage());
        }
    }

    private static void save() {
        if (historyFile == null) return;
        try (BufferedWriter writer =
                     Files.newBufferedWriter(historyFile, StandardCharsets.UTF_8)) {
            for (String kw : keywords) {
                writer.write(kw);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Không ghi được file lịch sử từ khóa: " + e.getMessage());
        }
    }

    public static void addKeyword(String keyword) {
        if (keyword == null) return;
        String trimmed = keyword.trim();
        if (trimmed.isEmpty()) return;
        if (keywords.add(trimmed)) save();
    }

    public static java.util.List<String> getSuggestions(String prefix) {
        java.util.List<String> result = new ArrayList<>();
        LinkedHashSet<String> unique = new LinkedHashSet<>();

        String base = (prefix == null) ? "" : prefix.trim();
        if (base.isEmpty()) return result;

        java.util.List<String> googleSug = SuggestService.fetchGoogleSuggestions(base);
        for (String s : googleSug) unique.add(s);

        String lower = base.toLowerCase();
        for (String kw : keywords) {
            if (kw.toLowerCase().startsWith(lower)) unique.add(kw);
        }

        int MAX = 40;
        for (String s : unique) {
            result.add(s);
            if (result.size() >= MAX) break;
        }
        return result;
    }
}
