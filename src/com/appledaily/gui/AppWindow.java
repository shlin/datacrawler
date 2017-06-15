/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.appledaily.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.UUID;

import javax.swing.JOptionPane;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import jdbc.mysql.MySQLConnector;

/**
 *
 * @author pstar
 */
public class AppWindow extends javax.swing.JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8194173309944752384L;
	private MySQLConnector connector;
	private HashSet<String> guidSet;

	/**
	 * Creates new form AppWindow
	 */
	public AppWindow() {
		this.guidSet = new HashSet<String>();
		initComponents();
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	// @SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed" desc="Generated
	// Code">//GEN-BEGIN:initComponents
	private void initComponents() {

		jPanel1 = new javax.swing.JPanel();
		jLabel2 = new javax.swing.JLabel();
		jLabel3 = new javax.swing.JLabel();
		jLabel4 = new javax.swing.JLabel();
		jLabel5 = new javax.swing.JLabel();
		textMysqlHost = new javax.swing.JTextField();
		textMysqlAccount = new javax.swing.JTextField();
		btnMysqlConnect = new javax.swing.JButton();
		btnMysqlConnect.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Connection conn;

				if (btnMysqlConnect.isEnabled()) {
					connector = new MySQLConnector(textMysqlHost.getText(), textMysqlAccount.getText(),
							String.valueOf(textMysqlPassword.getPassword()), textMysqlDatabase.getText());
					conn = connector.getConnection();

					if (conn != null) {
						textMysqlHost.setEnabled(false);
						textMysqlAccount.setEnabled(false);
						textMysqlPassword.setEnabled(false);
						textMysqlDatabase.setEnabled(false);
						btnMysqlConnect.setEnabled(false);

						try {
							ResultSet rs;
							Statement stat = conn.createStatement();

							// create table when table is not exist.
							stat.execute("CREATE TABLE IF NOT EXISTS `APPLEDAILY` (`ID` INT NOT NULL AUTO_INCREMENT,"
									+ "`URL_GUID` VARCHAR(40) NOT NULL," + "`URL` TEXT NOT NULL,"
									+ "`DATE` VARCHAR(8) NOT NULL," + "`TITLE` TEXT NOT NULL," + "`CONTENT` TEXT NULL,"
									+ "`KEYWORD` TEXT NULL," + "PRIMARY KEY (`ID`, `URL_GUID`),"
									+ "UNIQUE INDEX `ID_UNIQUE` (`ID` ASC),"
									+ "UNIQUE INDEX `URL_GUID_UNIQUE` (`URL_GUID` ASC)) CHARACTER SET utf8, COLLATE utf8_general_ci;");

							// query GUID list of news
							rs = stat.executeQuery("SELECT `URL_GUID` FROM `APPLEDAILY`");
							while (rs.next()) {
								guidSet.add(rs.getString(1));
							}

							JOptionPane.showMessageDialog(null, "資料庫連線成功！", "訊息", JOptionPane.INFORMATION_MESSAGE);
						} catch (SQLException e1) {
							JOptionPane.showMessageDialog(null, "資料庫連線失敗！", "錯誤", JOptionPane.ERROR_MESSAGE);
						}

					} else {
						JOptionPane.showMessageDialog(null, "資料庫連線失敗！", "錯誤", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		textMysqlPassword = new javax.swing.JPasswordField();
		textMysqlDatabase = new javax.swing.JTextField();
		jPanel2 = new javax.swing.JPanel();
		jLabel1 = new javax.swing.JLabel();
		textSearchKeyword = new javax.swing.JTextField();
		btnSearch = new javax.swing.JButton();
		btnSearch.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

				String strSdate = sdf.format(datetimeStart.getValue());
				String strEdate = sdf.format(datetimeEnd.getValue());

				// first search step.
				org.jsoup.Connection firstConn;
				Document firstDoc;
				firstConn = Jsoup.connect("http://search.appledaily.com.tw/appledaily/search");
				firstConn.data("searchMode", "Adv").data("searchType", "text").data("select", "AND");
				firstConn.data("querystrA", textSearchKeyword.getText()).data("sdate", strSdate).data("edate",
						strEdate);

				try {
					PreparedStatement stat = connector.getConnection().prepareStatement(
							"INSERT INTO `APPLEDAILY`(`URL_GUID`, `URL`, `DATE`, `TITLE`, `CONTENT`, `NULL`) VALUES(?, ?, ?, ?, ?, ?)");
					firstDoc = firstConn.post();

					btnSearch.setEnabled(false);
					for (Element link : firstDoc.select("#result>li")) {
						String strUrl = link.select("h2 a").first().absUrl("href");
						String strGUID = UUID.nameUUIDFromBytes(strUrl.getBytes()).toString();

						if (!guidSet.contains(strGUID)) {
							Document newsDoc = Jsoup.connect(strUrl).get();

							stat.setString(1, strGUID);
							stat.setString(2, strUrl);
							stat.setString(3, link.select("time").text());
							stat.setString(4, link.select("h2 a").text().trim());
							stat.setString(5, newsDoc.body().text().trim());
							stat.setString(6, textSearchKeyword.getText());

							stat.executeUpdate();

							Thread.sleep(500);
							guidSet.add(strGUID);
						}
					}

					JOptionPane.showMessageDialog(null, "資料下載完成！", "訊息", JOptionPane.INFORMATION_MESSAGE);
				} catch (IOException | InterruptedException | SQLException e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(null, "搜尋過程發生錯誤", "錯誤", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		jLabel6 = new javax.swing.JLabel();
		jLabel7 = new javax.swing.JLabel();
		datetimeStart = new javax.swing.JSpinner();
		datetimeEnd = new javax.swing.JSpinner();
		progressStatus = new javax.swing.JProgressBar();
		jLabel8 = new javax.swing.JLabel();

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		setTitle("蘋果日報新聞爬蟲");

		jPanel1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));

		jLabel2.setText("MySQL 連線IP：");

		jLabel3.setText("登入帳號：");
		jLabel3.setName(""); // NOI18N

		jLabel4.setText("登入密碼：");

		jLabel5.setText("資料庫名稱：");

		btnMysqlConnect.setText("連線");

		javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
		jPanel1.setLayout(jPanel1Layout);
		jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel1Layout.createSequentialGroup().addContainerGap()
						.addGroup(jPanel1Layout
								.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING).addComponent(jLabel3)
								.addComponent(jLabel2).addComponent(jLabel4).addComponent(jLabel5))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
								.addComponent(textMysqlHost).addComponent(textMysqlAccount)
								.addComponent(textMysqlPassword, javax.swing.GroupLayout.DEFAULT_SIZE, 150,
										Short.MAX_VALUE)
								.addComponent(textMysqlDatabase))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(btnMysqlConnect)
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel1Layout.createSequentialGroup().addContainerGap()
						.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jLabel2).addComponent(textMysqlHost,
										javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.PREFERRED_SIZE))
						.addGap(6, 6, 6)
						.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jLabel3).addComponent(textMysqlAccount,
										javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jLabel4).addComponent(textMysqlPassword,
										javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jLabel5).addComponent(btnMysqlConnect).addComponent(textMysqlDatabase,
										javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.PREFERRED_SIZE))
						.addContainerGap(9, Short.MAX_VALUE)));

		jLabel1.setText("搜尋關鍵字：");

		btnSearch.setText("搜尋");

		jLabel6.setText("時間區間-開始時間：");

		jLabel7.setText("時間區間-結束時間：");

		datetimeStart.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(1051804800000L),
				new java.util.Date(1051804800000L), null, java.util.Calendar.DAY_OF_MONTH));

		datetimeEnd.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(), null, new java.util.Date(),
				java.util.Calendar.DAY_OF_MONTH));

		progressStatus.setToolTipText("");
		progressStatus.setValue(50);
		progressStatus.setStringPainted(true);
		progressStatus.setVisible(false);

		jLabel8.setText("寫入資料庫-完成進度：");
		jLabel8.setVisible(false);

		javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
		jPanel2.setLayout(jPanel2Layout);
		jPanel2Layout
				.setHorizontalGroup(
						jPanel2Layout
								.createParallelGroup(
										javax.swing.GroupLayout.Alignment.LEADING)
								.addGroup(jPanel2Layout.createSequentialGroup().addContainerGap().addGroup(jPanel2Layout
										.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addGroup(jPanel2Layout.createSequentialGroup().addComponent(jLabel1)
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(textSearchKeyword, javax.swing.GroupLayout.PREFERRED_SIZE,
														200, javax.swing.GroupLayout.PREFERRED_SIZE)
												.addGap(9, 9, 9).addComponent(btnSearch))
										.addGroup(jPanel2Layout.createSequentialGroup().addGroup(jPanel2Layout
												.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
												.addComponent(jLabel8)
												.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout
														.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(jLabel6).addComponent(jLabel7)))
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
												.addGroup(jPanel2Layout
														.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(progressStatus,
																javax.swing.GroupLayout.PREFERRED_SIZE, 180,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(datetimeEnd,
																javax.swing.GroupLayout.PREFERRED_SIZE, 180,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addComponent(datetimeStart,
																javax.swing.GroupLayout.PREFERRED_SIZE, 180,
																javax.swing.GroupLayout.PREFERRED_SIZE))))
										.addGap(28, 28, 28)));
		jPanel2Layout.setVerticalGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanel2Layout.createSequentialGroup().addContainerGap()
						.addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jLabel1)
								.addComponent(textSearchKeyword, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(btnSearch))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jLabel6).addComponent(datetimeStart,
										javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jLabel7).addComponent(datetimeEnd, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(progressStatus, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(jLabel8))
						.addContainerGap(27, Short.MAX_VALUE)));

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
						Short.MAX_VALUE)
				.addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
						Short.MAX_VALUE));
		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jPanel2,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
								Short.MAX_VALUE)));

		pack();
	}// </editor-fold>//GEN-END:initComponents

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String args[]) {
		/* Set the Nimbus look and feel */
		// <editor-fold defaultstate="collapsed" desc=" Look and feel setting
		// code (optional) ">
		/*
		 * If Nimbus (introduced in Java SE 6) is not available, stay with the
		 * default look and feel. For details see
		 * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.
		 * html
		 */
		try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException ex) {
			java.util.logging.Logger.getLogger(AppWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			java.util.logging.Logger.getLogger(AppWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(AppWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(AppWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}
		// </editor-fold>

		/* Create and display the form */
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new AppWindow().setVisible(true);
			}
		});
	}

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton btnMysqlConnect;
	private javax.swing.JSpinner datetimeEnd;
	private javax.swing.JSpinner datetimeStart;
	private javax.swing.JButton btnSearch;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JLabel jLabel4;
	private javax.swing.JLabel jLabel5;
	private javax.swing.JLabel jLabel6;
	private javax.swing.JLabel jLabel7;
	private javax.swing.JLabel jLabel8;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JPanel jPanel2;
	private javax.swing.JProgressBar progressStatus;
	private javax.swing.JTextField textMysqlAccount;
	private javax.swing.JTextField textMysqlDatabase;
	private javax.swing.JTextField textMysqlHost;
	private javax.swing.JPasswordField textMysqlPassword;
	private javax.swing.JTextField textSearchKeyword;
	// End of variables declaration//GEN-END:variables
}
