package com.cj.utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class S3TransferWrapperTest {
    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void sendFileToS3() {
        S3TransferWrapper s3TransferWrapper = new S3TransferWrapper();
        s3TransferWrapper.run();

        //TODO:  add method to verify that file exists in S3.  If so, assertTrue(true).  If not, assertFalse(false).  Currently I manually open S3 to see if the file was transferred successfully.
    }

}