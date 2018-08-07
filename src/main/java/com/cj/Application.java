package com.cj;

import com.cj.config.Constants;
import com.cj.utils.S3TransferWrapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Date;
import java.util.Timer;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        //Set up timed task to send log records to S3 bucket at set interval.
        Date date = new Date();
        Timer timer = new Timer();
        timer.schedule(new S3TransferWrapper(), date, Constants.s3TimedTaskInterval);

        SpringApplication.run(Application.class, args);
    }

}