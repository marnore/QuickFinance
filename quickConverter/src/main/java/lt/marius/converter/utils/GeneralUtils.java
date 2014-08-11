package lt.marius.converter.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import lt.marius.converter.curview.Currency;
import lt.marius.converter.transactions.CurrencyStored;

public class GeneralUtils {
	
	public static double getValue(CurrencyStored c, Currency currency) {
		if (currency == null) {
			return c.getBaseDouble();
		}
		if (c.getCurrency().equals(currency)) {
			return c.getAmount().doubleValue();
		} else {
			return currency.getInverseValue(c.getBaseAmount()).doubleValue();
		}
	}
	
    public static void copyFile(InputStream is, File f) throws IOException {
    	f.getParentFile().mkdirs(); //create directory listing for file
//		f.setWritable(true, false);
		FileOutputStream fos = new FileOutputStream(f);
		
		byte[] buffer = new byte[1024];
		int read = 0;
		while ((read = is.read(buffer)) > 0) {
			fos.write(buffer, 0, read);
		}
		fos.flush();
		fos.close();
	}
    
    public static String encodeToMD5(String source) {
		String result = null;
		MessageDigest m = null;
		try {
			m = MessageDigest.getInstance("MD5");
			m.update(source.getBytes(), 0, source.length());
			result = new BigInteger(1, m.digest()).toString(16);
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		}
		return result;
	}

    public static void copyFile(File src, File dst) throws IOException {
        FileInputStream fis = new FileInputStream(src);
        FileChannel iChan = fis.getChannel();
        FileOutputStream fos = new FileOutputStream(dst);
        FileChannel oChan = fos.getChannel();
        iChan.transferTo(0, src.length(), oChan);
        iChan.close();
        oChan.close();
    }
}
