package com.fn.tiny

import java.security.MessageDigest
import java.text.DecimalFormat;

/**
 * TinyUtils
 * <p>
 * Author mmmy
 * Date 2019/7/23
 */
class TinyUtils {

    static String formatFileSize(long size) {
        DecimalFormat df = new DecimalFormat("#.00")
        if (size == 0L) return "0B"
        double dSize = size
        if (size < 1024) {
            return df.format(dSize) + "B"
        } else if (size < 1048576) {
            return df.format(dSize / 1024) + "KB"
        } else if (size < 1073741824) {
            return df.format(dSize / 1048576) + "MB"
        } else {
            return df.format(dSize / 1073741824) + "GB"
        }
    }

    static String generateFileMD5(File file) {
        MessageDigest digest = MessageDigest.getInstance("MD5")
        file.withInputStream() { is ->
            int read
            byte[] buffer = new byte[8192]
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read)
            }
        }
        byte[] md5sum = digest.digest()
        BigInteger bigInt = new BigInteger(1, md5sum)
        return bigInt.toString(16).padLeft(32, '0')
    }

    static void printLineSeparator() {
        printLineSeparator(1)
    }

    static void printLineSeparator(int lineCount) {
        for (int i = 0; i < lineCount; i++) {
            println(" ")
        }
    }

}
