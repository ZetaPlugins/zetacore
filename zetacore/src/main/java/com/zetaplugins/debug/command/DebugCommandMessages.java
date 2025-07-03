package com.zetaplugins.debug.command;

/**
 * DebugCommandMessages is a class that holds the messages used in the debug command.
 * It allows customization of the messages displayed to the user.
 */
public final class DebugCommandMessages {
    private String usageMessage = "&cUsage: /%command% <upload | generate>";
    private String noPermissionMessage = "&cYou do not have permission to use this command!";
    private String fileCreateSuccessMessage = "&8 [&a✔&8] &7Saved debug data to the following files:\n<click:COPY_TO_CLIPBOARD:%jsonPath%><#8b73f6>%jsonPath%</click>\n<click:COPY_TO_CLIPBOARD:%txtPath%><#8b73f6>%txtPath%</click>";
    private String failedToCreateFileMessage = "&cFailed to create debug report file: %error%";
    private String uploadConfirmMessage = "\n <#8b73f6>&lUploading Debug Report&r\n\n&7 Are you sure you want to upload the debug report? By confirming, you accept our <u><click:OPEN_URL:https://debug.zetaplugins.com/privacy>Privacy Policy</click></u>.\n\n <#8b73f6><click:RUN_COMMAND:%command%>[Click Here]</click> &r&8or run <u>%command%</u>\n";
    private String failToUploadMessage = "&cFailed to upload debug report: %error%";
    private String uploadSuccessMessage = "&8 [&a✔&8] &7Debug report uploaded successfully! You can view it here:\n <u><#8b73f6><click:OPEN_URL:%url%>%url%</click></u>\n";

    public String noPermissionMessage() {
        return noPermissionMessage;
    }

    public DebugCommandMessages setNoPermissionMessage(String noPermissionMessage) {
        this.noPermissionMessage = noPermissionMessage;
        return this;
    }

    public String usageMessage() {
        return usageMessage;
    }

    public DebugCommandMessages setUsageMessage(String usageMessage) {
        this.usageMessage = usageMessage;
        return this;
    }

    public String fileCreateSuccessMessage() {
        return fileCreateSuccessMessage;
    }

    public DebugCommandMessages setFileCreateSuccessMessage(String fileCreateSuccessMessage) {
        this.fileCreateSuccessMessage = fileCreateSuccessMessage;
        return this;
    }

    public String failedToCreateFileMessage() {
        return failedToCreateFileMessage;
    }

    public DebugCommandMessages setFailedToCreateFileMessage(String failedToCreateFileMessage) {
        this.failedToCreateFileMessage = failedToCreateFileMessage;
        return this;
    }

    public String uploadConfirmMessage() {
        return uploadConfirmMessage;
    }

    public DebugCommandMessages setUploadConfirmMessage(String uploadConfirmMessage) {
        this.uploadConfirmMessage = uploadConfirmMessage;
        return this;
    }

    public String failToUploadMessage() {
        return failToUploadMessage;
    }

    public DebugCommandMessages setFailToUploadMessage(String failToUploadMessage) {
        this.failToUploadMessage = failToUploadMessage;
        return this;
    }

    public String uploadSuccessMessage() {
        return uploadSuccessMessage;
    }

    public DebugCommandMessages setUploadSuccessMessage(String uploadSuccessMessage) {
        this.uploadSuccessMessage = uploadSuccessMessage;
        return this;
    }
}
