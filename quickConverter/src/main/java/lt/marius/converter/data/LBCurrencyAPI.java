package lt.marius.converter.data;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import lt.marius.converter.curview.Currency;
import lt.marius.converter.data.CurrencyProvider.IsoCode;
import lt.marius.converter.network.NetworkUtils;

public class LBCurrencyAPI implements CurrencyAPI {

	private static final String url = "http://www.lb.lt/webservices/FxRates/FxRates.asmx/getFxRates?tp=eu&dt=%s";
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
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			InputStream input = NetworkUtils.getRemoteStream(String.format(url, sdf.format(new Date())), params);
			XMLReader reader;
			try {
				reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
				ParseHandler handler = new ParseHandler();
				reader.setContentHandler(handler);
				reader.parse(new InputSource(input));
				input.close();
                baseMap.put("EUR", new Currency(BigDecimal.ONE, "EUR", NAME_MAPPINGS.get("EUR")));
                baseMap.put("LTL", new Currency(BigDecimal.ONE.divide(new BigDecimal(3.4528d), 20, BigDecimal.ROUND_UP), "LTL", NAME_MAPPINGS.get("LTL")));
				return baseMap.get(currency);
			} catch (SAXException | ParserConfigurationException | IOException e) {
				e.printStackTrace();
			}
            return null;
		}
		
	}
	
	private class ParseHandler extends DefaultHandler {
		
		private Currency curr;
		private StringBuilder sb;

        private double baseAmount, amount;

        private String otherCurrCode;

        private boolean readingCcyAmount = false;
        private boolean readingBaseAmount = false;

		
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			if (sb != null) {
				sb.append(ch, start, length);
			}
		}
		
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if (qName.equals("FxRate")) {
				curr = new Currency();

                baseAmount = -1;
                amount = -1;
			}  else if (qName.equals("CcyAmt")) {
                readingCcyAmount = true;

            } else if (qName.equals("Ccy")) {
                sb = new StringBuilder();
            } else if (qName.equals("Amt")) {
                sb = new StringBuilder();
            }
		}
		
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (curr == null) {
				return;
			}
			if (qName.equals("FxRate")) {
                if (amount > 0 && baseAmount > 0) {
                    curr = new Currency(
                            BigDecimal.ONE.divide(new BigDecimal(amount / baseAmount), 20, RoundingMode.UP),
                            otherCurrCode, NAME_MAPPINGS.get(otherCurrCode));
                    baseMap.put(curr.getShortCode(), curr);
                }
			} else if (qName.equals("CcyAmt")) {

			} else if (qName.equals("Ccy")) {
                if (sb.toString().equals("EUR")) {
                    readingBaseAmount = true;
                } else {
                    readingBaseAmount = false;
                    otherCurrCode = sb.toString();
                }
                sb = null;
            } else if (qName.equals("Amt")) {
                if (readingBaseAmount) {
                    baseAmount = Double.parseDouble(sb.toString());
                } else {
                    amount = Double.parseDouble(sb.toString());
                }
                sb = null;
            }

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

	private final IsoCode[] SUPPORTED_CODES = {
            IsoCode.DZD,
            IsoCode.ARS,
            IsoCode.AUD,
            IsoCode.BHD,
            IsoCode.BDT,
            IsoCode.BOB,
            IsoCode.BAM,
            IsoCode.BRL,
            IsoCode.GBP,
            IsoCode.BGN,
            IsoCode.CAD,
            IsoCode.CLP,
            IsoCode.CNY,
            IsoCode.COP,
            IsoCode.HRK,
            IsoCode.CZK,
            IsoCode.DKK,
            IsoCode.EGP,
            IsoCode.EUR,
            IsoCode.HKD,
            IsoCode.HUF,
            IsoCode.INR,
            IsoCode.IDR,
            IsoCode.ILS,
            IsoCode.JPY,
            IsoCode.JOD,
            IsoCode.KZT,
            IsoCode.KES,
            IsoCode.KWD,
            IsoCode.LBP,
            IsoCode.LTL,
            IsoCode.MKD,
            IsoCode.MYR,
            IsoCode.MUR,
            IsoCode.MXN,
            IsoCode.MDL,
            IsoCode.MAD,
            IsoCode.TWD,
            IsoCode.NZD,
            IsoCode.NGN,
            IsoCode.NOK,
            IsoCode.PKR,
            IsoCode.PEN,
            IsoCode.PHP,
            IsoCode.PLN,
            IsoCode.QAR,
            IsoCode.RON,
            IsoCode.RUB,
            IsoCode.SAR,
            IsoCode.RSD,
            IsoCode.SGD,
            IsoCode.ZAR,
            IsoCode.KRW,
            IsoCode.LKR,
            IsoCode.SEK,
            IsoCode.CHF,
            IsoCode.TZS,
            IsoCode.THB,
            IsoCode.TND,
            IsoCode.TRY,
            IsoCode.UAH,
            IsoCode.AED,
            IsoCode.USD,
            IsoCode.UYU,
            IsoCode.UZS,
            IsoCode.VEF,
            IsoCode.VND,
            IsoCode.XOF,
            IsoCode.YER,
    };

    private static final Map<String, String> NAME_MAPPINGS = new HashMap<>();
    static
    {
        NAME_MAPPINGS.put("AED", "United Arab Emirates dirham");
        NAME_MAPPINGS.put("ARS", "Argentine peso");
        NAME_MAPPINGS.put("AUD", "Australian dollar");
        NAME_MAPPINGS.put("BAM", "Bosnia and Herzegovina convertible mark");
        NAME_MAPPINGS.put("BDT", "Bangladeshi taka");
        NAME_MAPPINGS.put("BGN", "Bulgarian lev");
        NAME_MAPPINGS.put("BHD", "Bahraini dinar");
        NAME_MAPPINGS.put("BOB", "Bolivian boliviano");
        NAME_MAPPINGS.put("BRL", "Brazilian real");
        NAME_MAPPINGS.put("CAD", "Canadian dollar");
        NAME_MAPPINGS.put("CHF", "Swiss franc");
        NAME_MAPPINGS.put("CLP", "Chilean peso");
        NAME_MAPPINGS.put("CNY", "Chinese yuan");
        NAME_MAPPINGS.put("COP", "Colombian peso");
        NAME_MAPPINGS.put("CZK", "Czech koruna");
        NAME_MAPPINGS.put("DKK", "Danish krone");
        NAME_MAPPINGS.put("DZD", "Algerian dinar");
        NAME_MAPPINGS.put("EGP", "Egyptian pound");
        NAME_MAPPINGS.put("EUR", "Euro");
        NAME_MAPPINGS.put("GBP", "British pound");
        NAME_MAPPINGS.put("HKD", "Hong Kong dollar");
        NAME_MAPPINGS.put("HRK", "Croatian kuna");
        NAME_MAPPINGS.put("HUF", "Hungarian forint");
        NAME_MAPPINGS.put("IDR", "Indonesian rupiah");
        NAME_MAPPINGS.put("ILS", "Israeli new shekel");
        NAME_MAPPINGS.put("INR", "Indian rupee");
        NAME_MAPPINGS.put("JOD", "Jordanian dinar");
        NAME_MAPPINGS.put("JPY", "Japanese yen");
        NAME_MAPPINGS.put("KES", "Kenyan shilling");
        NAME_MAPPINGS.put("KRW", "South Korean won");
        NAME_MAPPINGS.put("KWD", "Kuwaiti dinar");
        NAME_MAPPINGS.put("KZT", "Kazakhstani tenge");
        NAME_MAPPINGS.put("LBP", "Lebanese pound");
        NAME_MAPPINGS.put("LKR", "Sri Lankan rupee");
        NAME_MAPPINGS.put("LTL", "Lithuanian litas");
        NAME_MAPPINGS.put("MAD", "Moroccan dirham");
        NAME_MAPPINGS.put("MDL", "Moldovan leu");
        NAME_MAPPINGS.put("MKD", "Macedonian denar");
        NAME_MAPPINGS.put("MUR", "Mauritian rupee");
        NAME_MAPPINGS.put("MXN", "Mexican peso");
        NAME_MAPPINGS.put("MYR", "Malaysian ringgit");
        NAME_MAPPINGS.put("NGN", "Nigerian naira");
        NAME_MAPPINGS.put("NOK", "Norwegian krone");
        NAME_MAPPINGS.put("NZD", "New Zealand dollar");
        NAME_MAPPINGS.put("PEN", "Peruvian nuevo sol");
        NAME_MAPPINGS.put("PHP", "Philippine peso");
        NAME_MAPPINGS.put("PKR", "Pakistani rupee");
        NAME_MAPPINGS.put("PLN", "Polish złoty");
        NAME_MAPPINGS.put("QAR", "Qatari riyal");
        NAME_MAPPINGS.put("RON", "Romanian leu");
        NAME_MAPPINGS.put("RSD", "Serbian dinar");
        NAME_MAPPINGS.put("RUB", "Russian ruble");
        NAME_MAPPINGS.put("SAR", "Saudi riyal");
        NAME_MAPPINGS.put("SEK", "Swedish krona");
        NAME_MAPPINGS.put("SGD", "Singapore dollar");
        NAME_MAPPINGS.put("THB", "Thai baht");
        NAME_MAPPINGS.put("TND", "Tunisian dinar");
        NAME_MAPPINGS.put("TRY", "Turkish lira");
        NAME_MAPPINGS.put("TWD", "New Taiwan dollar");
        NAME_MAPPINGS.put("TZS", "Tanzanian shilling");
        NAME_MAPPINGS.put("UAH", "Ukrainian hryvnia");
        NAME_MAPPINGS.put("USD", "United States dollar");
        NAME_MAPPINGS.put("UYU", "Uruguayan peso");
        NAME_MAPPINGS.put("UZS", "Uzbekistani som");
        NAME_MAPPINGS.put("VEF", "Venezuelan bolívar");
        NAME_MAPPINGS.put("VND", "Vietnamese đồng");
        NAME_MAPPINGS.put("XOF", "West African CFA franc");
        NAME_MAPPINGS.put("YER", "Yemeni rial");
        NAME_MAPPINGS.put("ZAR", "South African rand");
    }


}
