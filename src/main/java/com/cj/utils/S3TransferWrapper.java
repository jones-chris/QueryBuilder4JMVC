//package com.cj.utils;
//
//import com.amazonaws.AmazonServiceException;
//import com.amazonaws.SdkClientException;
//import com.amazonaws.auth.AWSCredentials;
//import com.amazonaws.auth.BasicAWSCredentials;
//import com.amazonaws.services.s3.AmazonS3;
//import com.amazonaws.services.s3.AmazonS3Client;
//import com.amazonaws.services.s3.model.ObjectMetadata;
//import com.amazonaws.services.s3.model.PutObjectRequest;
//
//import java.io.File;
//import java.io.FileWriter;
//import java.io.InputStream;
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.ResultSet;
//import java.util.Date;
//import java.util.Properties;
//import java.util.TimerTask;
//
//
//public class S3TransferWrapper extends TimerTask {
//
//    @Override
//    public void run() {
//        final String COMMA_DELIMITER = ",";
//        final String CARRIAGE_RETURN = "\n";
//        final String PROJECT_ROOT_DIRECTORY = System.getProperty("user.dir");
//        final String FILE_PATH = PROJECT_ROOT_DIRECTORY + "\\data\\log.csv";
//
//        File file = new File(FILE_PATH);
//        try (FileWriter fileWriter = new FileWriter(file, false);
//             Connection conn = DriverManager.getConnection("jdbc:sqlite:./data/logging.db");
//             ResultSet rs = conn.createStatement().executeQuery("select * from log;")) {
//
//            fileWriter.append("TIMESTAMP,SELECT_STATEMENT_TO_STRING,SELECT_STATEMENT_TO_SQL,DATABASE_STILL_EXISTS,TABLE_STILL_EXISTS," +
//                    "NUM_OF_TABLES_IS_SAME,NUM_OF_COLUMNS_IN_TABLE_IS_SAME,NUM_OF_ROWS_IN_TABLE_IS_SAME,TABLE_DATA_IS_SAME," +
//                    "USER_TABLE_PERMISSIONS_ARE_SAME").append(CARRIAGE_RETURN);
//
//            while (rs.next()) {
//                fileWriter.append(rs.getString("timestamp"));
//                fileWriter.append(COMMA_DELIMITER);
//                fileWriter.append(rs.getString("select_statement_to_string"));
//                fileWriter.append(COMMA_DELIMITER);
//                fileWriter.append(rs.getString("select_statement_to_sql"));
//                fileWriter.append(COMMA_DELIMITER);
//                fileWriter.append(rs.getString("database_still_exists"));
//                fileWriter.append(COMMA_DELIMITER);
//                fileWriter.append(rs.getString("table_still_exists"));
//                fileWriter.append(COMMA_DELIMITER);
//                fileWriter.append(rs.getString("num_of_tables_is_same"));
//                fileWriter.append(COMMA_DELIMITER);
//                fileWriter.append(rs.getString("num_of_cols_in_table_is_same"));
//                fileWriter.append(COMMA_DELIMITER);
//                fileWriter.append(rs.getString("num_of_rows_in_table_is_same"));
//                fileWriter.append(COMMA_DELIMITER);
//                fileWriter.append(rs.getString("table_data_is_same"));
//                fileWriter.append(COMMA_DELIMITER);
//                fileWriter.append(rs.getString("user_table_permissions_are_same"));
//                fileWriter.append(COMMA_DELIMITER);
//                fileWriter.append(CARRIAGE_RETURN);
//            }
//
//            fileWriter.flush();
//            fileWriter.close();
//
//            // Delete records from database since they were exported to csv.
//            conn.createStatement().execute("delete from log;");
//
//            String clientRegion = "us-east-1";
//            String bucketName = "jones-chris-aleph-tav";
//            String fileKey = "log_" + new Date() + ".csv";
//
//            try {
//                Properties props = new Properties();
//                InputStream input = this.getClass().getClassLoader().getResourceAsStream("application.properties");
//                props.load(input);
//
//                AWSCredentials credentials = new BasicAWSCredentials(props.getProperty("aws.accessKey"),
//                        props.getProperty("aws.secretKey"));
//
////                AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
////                        .withRegion(clientRegion)
////                        .withCredentials(new ProfileCredentialsProvider())
////                        .build();
//                  AmazonS3 s3Client = new AmazonS3Client(credentials);
//
//                // Upload a file as a new object with ContentType and title specified.
//                PutObjectRequest request = new PutObjectRequest(bucketName, fileKey, new File(FILE_PATH));
//                ObjectMetadata metadata = new ObjectMetadata();
//                metadata.setContentType("plain/text");
//                metadata.addUserMetadata("x-amz-meta-title", "someTitle");
//                request.setMetadata(metadata);
//                s3Client.putObject(request);
//            }
//            catch(AmazonServiceException e) {
//                // The call was transmitted successfully, but Amazon S3 couldn't process
//                // it, so it returned an error response.
//                e.printStackTrace();
//            }
//            catch(SdkClientException e) {
//                // Amazon S3 couldn't be contacted for a response, or the client
//                // couldn't parse the response from Amazon S3.
//                e.printStackTrace();
//            }
//
//        } catch (Exception ex ) {
//            ex.printStackTrace();
//        }
//    }
//}
