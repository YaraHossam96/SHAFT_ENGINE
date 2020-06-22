package com.shaft.gui.element;

import com.shaft.gui.video.RecordManager;
import com.shaft.tools.io.ReportManager;
import com.shaft.tools.support.JSHelpers;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchSessionException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Objects;

public class JavaScriptWaitManager {
    private static final int WAIT_DURATION_INTEGER = 15;
    private static final String TARGET_DOCUMENT_READY_STATE = "complete";
    private static final ThreadLocal<WebDriver> jsWaitDriver = new ThreadLocal<>();
    private static final int delayBetweenPolls = 20; // milliseconds
    private static JavascriptExecutor jsExec;

    private JavaScriptWaitManager() {
        throw new IllegalStateException("Utility class");
    }

    public static void setDriver(WebDriver driver) {
        jsWaitDriver.set(driver);
        jsExec = (JavascriptExecutor) jsWaitDriver.get();
    }

    /**
     * Waits for jQuery, Angular, and/or Javascript if present on the current page.
     *
     * @return true in case waiting didn't face any isssues, and false in case of a
     * severe exception
     */
    public static boolean waitForLazyLoading() {
        RecordManager.startVideoRecording();
        try {
            waitForJQueryLoadIfDefined();
            waitForAngularIfDefined();
            waitForJSLoadIfDefined();
            return true;
        } catch (NoSuchSessionException | NullPointerException e) {
            // do nothing
            return true;
        } catch (WebDriverException e) {
            if (e.getMessage().contains("jQuery is not defined")) {
                // do nothing
                return true;
            } else {
                ReportManager.log(e);
                return true;
            }
        } catch (Exception e) {
            if (e.getMessage().contains("Error communicating with the remote browser. It may have died.")) {
                ReportManager.log(e);
                return false;
            } else {
                ReportManager.log(e);
                return true;
            }
        }
    }

    private static void waitForJQueryLoadIfDefined() {
        Boolean jQueryDefined = (Boolean) jsExec.executeScript("return typeof jQuery != 'undefined'");
        if (Boolean.TRUE.equals(jQueryDefined)) {
            ExpectedCondition<Boolean> jQueryLoad = null;
            try {
                // Wait for jQuery to load
                jQueryLoad = driver -> ((Long) ((JavascriptExecutor) jsWaitDriver.get())
                        .executeScript("return jQuery.active") == 0);
            } catch (NullPointerException e) {
                // do nothing
            }
            // Get JQuery is Ready
            boolean jqueryReady = (Boolean) jsExec.executeScript("return jQuery.active==0");

            if (!jqueryReady) {
                // Wait JQuery until it is Ready!
                int tryCounter = 0;
                while ((!jqueryReady) && (tryCounter < 5)) {
                    try {
                        // Wait for jQuery to load
                        (new WebDriverWait(jsWaitDriver.get(), WAIT_DURATION_INTEGER)).until(jQueryLoad);
                    } catch (NullPointerException e) {
                        // do nothing
                    }
                    sleep(delayBetweenPolls);
                    tryCounter++;
                    jqueryReady = (Boolean) jsExec.executeScript("return jQuery.active == 0");
                }
            }
        }
    }

    private static void waitForAngularLoad() {
        JavascriptExecutor jsExec = (JavascriptExecutor) jsWaitDriver.get();

        String angularReadyScript = "return angular.element(document).injector().get('$http').pendingRequests.length === 0";

        // Wait for ANGULAR to load
        ExpectedCondition<Boolean> angularLoad = driver -> Boolean
                .valueOf(((JavascriptExecutor) Objects.requireNonNull(driver)).executeScript(angularReadyScript).toString());

        // Get Angular is Ready
        boolean angularReady = Boolean.parseBoolean(jsExec.executeScript(angularReadyScript).toString());

        if (!angularReady) {
            // Wait ANGULAR until it is Ready!
            int tryCounter = 0;
            while ((!angularReady) && (tryCounter < 5)) {
                // Wait for Angular to load
                (new WebDriverWait(jsWaitDriver.get(), WAIT_DURATION_INTEGER)).until(angularLoad);
                // More Wait for stability (Optional)
                sleep(delayBetweenPolls);
                tryCounter++;
                angularReady = Boolean.parseBoolean(jsExec.executeScript(angularReadyScript).toString());
            }
        }
    }

    private static void waitForJSLoadIfDefined() {
        JavascriptExecutor jsExec = (JavascriptExecutor) jsWaitDriver.get();

        // Wait for Javascript to load
        ExpectedCondition<Boolean> jsLoad = driver -> ((JavascriptExecutor) jsWaitDriver.get())
                .executeScript(JSHelpers.DOCUMENT_READYSTATE.getValue()).toString().trim()
                .equalsIgnoreCase(TARGET_DOCUMENT_READY_STATE);

        // Get JS is Ready
        boolean jsReady = jsExec.executeScript(JSHelpers.DOCUMENT_READYSTATE.getValue()).toString().trim()
                .equalsIgnoreCase(TARGET_DOCUMENT_READY_STATE);

        // Wait Javascript until it is Ready!
        if (!jsReady) {
            // Wait JS until it is Ready!
            int tryCounter = 0;
            while ((!jsReady) && (tryCounter < 5)) {
                // Wait for Javascript to load
                try {
                    (new WebDriverWait(jsWaitDriver.get(), WAIT_DURATION_INTEGER)).until(jsLoad);
                } catch (org.openqa.selenium.TimeoutException e) {
                    //do nothing
                    //TODO: confirm that this fixed the timeout issue on the grid
                }
                // More Wait for stability (Optional)
                sleep(delayBetweenPolls);
                tryCounter++;
                jsReady = jsExec.executeScript(JSHelpers.DOCUMENT_READYSTATE.getValue()).toString().trim()
                        .equalsIgnoreCase(TARGET_DOCUMENT_READY_STATE);
            }
        }
    }

    private static void waitForAngularIfDefined() {
        try {
            Boolean angularDefined = !((Boolean) jsExec.executeScript("return window.angular === undefined"));
            if (Boolean.TRUE.equals(angularDefined)) {
                Boolean angularInjectorDefined = !((Boolean) jsExec
                        .executeScript("return angular.element(document).injector() === undefined"));

                if (Boolean.TRUE.equals(angularInjectorDefined)) {
                    waitForAngularLoad();
                }
            }
        } catch (WebDriverException e) {
            // do nothing
        }
    }

    private static void sleep(Integer milliSeconds) {
        long secondsLong = (long) milliSeconds;
        try {
            Thread.sleep(secondsLong);
        } catch (Exception e) {
            ReportManager.log(e);
            // InterruptedException
        }
    }
}