package lt.marius.converter.data;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import lt.marius.converter.curview.Currency;
import lt.marius.converter.data.CurrencyProvider.IsoCode;
import lt.marius.converter.network.NetworkUtils;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class TheMoneyConverterCurrencyAPI implements CurrencyAPI {

	private static final String url = "http://themoneyconverter.com/rss-feed/%s/rss.xml";
	private static final int RETRIES = 2;	//times to retry if currency is not found
	
	private HashMap<String, Currency> baseMap = new HashMap<String, Currency>();
	private int tries = 0;
	
	@Override
	public Currency getCurrencyValue(String baseCurrency, String currency) {
		if (baseMap.containsKey(currency)) {
			return baseMap.get(currency);
		} else if (tries >= RETRIES) {
			System.out.println("Could not find currency for " + currency);
			return null;
		} else {
			tries++;
			System.out.println("Downloading all");
			Map<String, String> params = new HashMap<String, String>();
			params.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
//			params.put("Accept-language", "fr,en;q=0.5");
			InputStream input = NetworkUtils.getRemoteStream(String.format(url, baseCurrency), params);
			XMLReader reader;
			try {
				reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
				ParseHandler handler = new ParseHandler();
				reader.setContentHandler(handler);
				reader.parse(new InputSource(input));
				input.close();
				return baseMap.get(currency);
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
		
	}
	
	private class ParseHandler extends DefaultHandler {
		
		private Currency curr;
		private StringBuilder sb;
		
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			if (sb != null) {
				sb.append(ch, start, length);
			}
		}
		
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if (qName.equals("item")) {
				curr = new Currency();
				sb = new StringBuilder();
			} 
		}
		
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (curr == null) {
				return;
			}
			if (qName.equals("title")) {
				curr.setCurrencyCodeShort(sb.toString().trim().split("/")[0]);
			} else if (qName.equals("description")) {
				String desc = sb.toString();
				int st = desc.indexOf('=');
				desc = desc.substring(st + 2);
				double value = parseDouble(desc.substring(0, desc.indexOf(' ')));
				desc = desc.substring(desc.indexOf(' ') + 1).trim();
				curr.updateValue(BigDecimal.ONE.divide(new BigDecimal(value), 20, RoundingMode.UP));
				curr.setCurrencyTitle(desc);
			} else if (qName.equals("item")) {
				baseMap.put(curr.getShortCode(), curr);
			}
			sb = new StringBuilder();
		}
		
		private double parseDouble(String number) {
			if (number.indexOf(',') != -1) {
				String[] parts = number.split(",");
				String str = "";
				for (String s: parts) {
					str += s;
				}
				number = str;
			}
			return Double.parseDouble(number);
			
		}
		
	}

	@Override
	public IsoCode[] getSupportedIsoCodes() {
		return SUPPORTED_CODES;
	}

	private final IsoCode[] SUPPORTED_CODES = {IsoCode.AED,
			IsoCode.ARS,
			IsoCode.AUD,
//			IsoCode.AWG,
//			IsoCode.BAM,
//			IsoCode.BBD,
//			IsoCode.BDT,
			IsoCode.BGN,
			IsoCode.BHD,
//			IsoCode.BMD,
			IsoCode.BOB,
			IsoCode.BRL,
//			IsoCode.BSD,
			IsoCode.CAD,
			IsoCode.CHF,
			IsoCode.CLP,
			IsoCode.CNY,
			IsoCode.COP,
			IsoCode.CZK,
			IsoCode.DKK,
			IsoCode.EGP,
			IsoCode.EUR,
			IsoCode.FJD,
			IsoCode.GBP,
//			IsoCode.GHS,
//			IsoCode.GMD,
//			IsoCode.GTQ,
			IsoCode.HKD,
			IsoCode.HRK,
			IsoCode.HUF,
			IsoCode.IDR,
			IsoCode.ILS,
			IsoCode.INR,
//			IsoCode.ISK,
			IsoCode.JMD,
			IsoCode.JOD,
			IsoCode.JPY,
			IsoCode.KES,
//			IsoCode.KHR,
			IsoCode.KRW,
			IsoCode.KWD,
//			IsoCode.LAK,
			IsoCode.LBP,
			IsoCode.LKR,
			IsoCode.LTL,
			IsoCode.LVL,
			IsoCode.MAD,
			IsoCode.MDL,
//			IsoCode.MGA,
			IsoCode.MKD,
			IsoCode.MUR,
//			IsoCode.MVR,
			IsoCode.MXN,
			IsoCode.MYR,
			IsoCode.NAD,
			IsoCode.NGN,
			IsoCode.NOK,
			IsoCode.NPR,
			IsoCode.NZD,
			IsoCode.OMR,
//			IsoCode.PAB,
			IsoCode.PEN,
			IsoCode.PHP,
			IsoCode.PKR,
			IsoCode.PLN,
			IsoCode.PYG,
			IsoCode.QAR,
			IsoCode.RON,
			IsoCode.RSD,
			IsoCode.RUB,
			IsoCode.SAR,
			IsoCode.SCR,
			IsoCode.SEK,
			IsoCode.SGD,
//			IsoCode.SYP,
			IsoCode.THB,
			IsoCode.TND,
			IsoCode.TRY,
			IsoCode.TWD,
			IsoCode.UAH,
			IsoCode.UGX,
			IsoCode.USD,
			IsoCode.UYU,
			IsoCode.VEF,
			IsoCode.VND,
//			IsoCode.XAF,
//			IsoCode.XCD,
//			IsoCode.XOF,
//			IsoCode.XPF,
			IsoCode.ZAR};
	
}
