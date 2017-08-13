import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;

import org.apache.commons.codec.binary.Base64;

public class __ShowQrFileInConsole {
	String[] QR_Code_Titels = { "", "ZDA: ", "Kassen-ID:", "Belegnummer:", "Beleg-Datum-Uhrzeit:",
			"Betrag-Satz-Normal:", "Betrag-Satz-Ermaessigt-1:", "Betrag-Satz-Ermaessigt-2:", "Betrag-Satz-Null:",
			"Betrag-Satz-Besonders:", "Stand-Umsatz-Zaehler-AES256-ICM_Entschlüsselt:", "Zertifikat-Seriennummer:",
			"Sig-Voriger-Beleg:", "Signatur:", "", "", "" }; // Array mit die vor einem Wert bei der "ShowDEP" und "schowQR" methode angezeigt werden
	//coding
	__Coding code = new __Coding();
	__ReadFile read = new __ReadFile();
	

	public String show(String show_4, String show_5) {
		StringBuilder outputstring = new StringBuilder();
		try {
			String Input = read.Readtxt(show_4);

			Input = Input.substring(1);
			Input = Input.substring(0, Input.length() - 1);
			String[] parts = Input.split("\"");
			int Flag = 0;
			while (Flag < parts.length) {
				if (parts[Flag].length() > 5) {
					String[] parts2 = parts[Flag].split("_");
					int Flag2 = 0;
					String KassenID = "";
					String BelegID = "";
					while (Flag2 < parts2.length) {
						if (Flag2 == 2) {
							KassenID = parts2[Flag2];
						}
						if (Flag2 == 3) {
							BelegID = parts2[Flag2];
						}
						if (Flag2 == 10) {
							outputstring.append(
									"Stand-Umsatz-Zaehler-AES256-ICM_Verschlüsselt: " + parts2[Flag2] + "   \r\n");
							// Abfrage ob STO oder TRA (Trainings beleg oder
							// Storno Beleg)
							// oder Umsatz wert
							if (parts2[Flag2].equals("U1RP")) {
								String d = "STO";
								outputstring.append(QR_Code_Titels[Flag2] + "  " + d + "   \r\n");
							} else if (parts2[Flag2].equals("VFJB")) {
								String d = "TRA";
								outputstring.append(QR_Code_Titels[Flag2] + "  " + d + "  \r\n");
							} else {
								long i1 = code.CalcNewValue(KassenID, BelegID, parts2[Flag2],show_5);
								double d = (double) i1;
								d = d / 100;
								outputstring.append(QR_Code_Titels[Flag2] + "  " + d + "€   \r\n");
							}
						} else {
							outputstring.append(QR_Code_Titels[Flag2] + " " + parts2[Flag2] + "\r\n");
							// calculate Sig-Vorig-btertag for the next value
							if (Flag2 == 12) {
								String retun = code.GenerateJWSSig(parts[Flag]);

								MessageDigest md1 = MessageDigest.getInstance("sha-256");
								md1.update(retun.getBytes());
								byte[] digest1 = md1.digest();

								// extract number of bytes (N, defined in
								// RKsuite)
								// from
								// hash value
								int bytesToExtract1 = 8;
								byte[] conDigest1 = new byte[bytesToExtract1];
								System.arraycopy(digest1, 0, conDigest1, 0, bytesToExtract1);

								// encode value as BASE64 String ==> chainValue
								byte[] Sig_Nae_Beleg = Base64.encodeBase64(conDigest1, false);
								String Sig_Nae_Beleg_String = new String(Sig_Nae_Beleg, "UTF-8");
								outputstring.append("Sig_Nächster_Beleg_Calculated: " + Sig_Nae_Beleg_String + "\r\n");
							}

						}
						Flag2++;
					}

					// QR_Code_Titels
				}
				Flag++;
			}
		} catch (IOException | NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return outputstring.toString();
	}
}
