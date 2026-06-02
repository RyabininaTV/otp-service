package com.promoit.otp.scheduler;

import com.promoit.otp.dao.OtpDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class OtpExpiryScheduler {
    private static final Logger logger = LoggerFactory.getLogger(OtpExpiryScheduler.class);
    private final OtpDao otpDao;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public OtpExpiryScheduler(OtpDao otpDao) {
        this.otpDao = otpDao;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                otpDao.expireOldCodes();
                logger.debug("Expired old OTP codes");
            } catch (Exception e) {
                logger.error("Error expiring old OTP codes", e);
            }
        }, 1, 1, TimeUnit.MINUTES);

        logger.info("OTP expiry scheduler started");
    }

    public void stop() {
        scheduler.shutdown();
        logger.info("OTP expiry scheduler stopped");
    }
}