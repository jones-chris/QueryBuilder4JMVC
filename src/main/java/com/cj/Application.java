package com.cj;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        //Set up timed task to send log records to S3 bucket at set interval.
//        Date date = new Date();
//        Timer timer = new Timer();
//        timer.schedule(new S3TransferWrapper(), date, Constants.s3TimedTaskInterval);

        for (String arg : args) {
            System.out.println(arg);
        }

        SpringApplication.run(Application.class, args);
    }

}