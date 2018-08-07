package com.cj.utils;

import com.querybuilder4j.sqlbuilders.statements.SelectStatement;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Properties;

public class EmailWrapper {

    /**
     * Sends an email to querybuilder4j@gmail.com.  The parameters contribute to creating the email's text.
     *
     * @param stmt
     * @param databaseAuditResults
     * @throws MessagingException
     */
    public static void sendEmail(SelectStatement stmt, Map<String, Boolean> databaseAuditResults) throws MessagingException {
        String subject = "A querybuilder4j check failed";
        String to = "querybuilder4j@gmail.com";
        String from = "querybuilder4j@gmail.com";
        String host = "localhost";
        Properties props = System.getProperties();
        props.setProperty("mail.smtp.host", host);

        Session session = Session.getDefaultInstance(props);

        MimeMessage message = new MimeMessage(session);

        message.setFrom(new InternetAddress(from));

        message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

        message.setSubject(subject);

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String text = String.format("Timestamp:  %s \n" +
                                    "Select Statement object:  %s \n" +
                                    "Select Statement SQL:  %s \n" +
                                    "Database Exists?:  %s \n" +
                                    "Table Exists?:  %s \n" +
                                    "Tables Are Same:  %s \n" +
                                    "Number Of Table Columns Are Same:  %s \n" +
                                    "Number Of Table Rows Are Same:  %s \n" +
                                    "Table Data Is Same:  %s \n" +
                                    "Number Of Users Is Same:  %s \n",
                                    timestamp,
                                    stmt.toString(),
                                    stmt.toSql(),
                                    databaseAuditResults.get("databaseExists"),
                                    databaseAuditResults.get("tableExists"),
                                    databaseAuditResults.get("tablesAreSame"),
                                    databaseAuditResults.get("numOfTableColumnsAreSame"),
                                    databaseAuditResults.get("numOfTableRowsAreSame"),
                                    databaseAuditResults.get("tableDataIsSame"),
                                    databaseAuditResults.get("numOfUsersIsSame"));
        message.setText(text);

        Transport.send(message);
    }
}
