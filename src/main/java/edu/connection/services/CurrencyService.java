package edu.connection.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Service de gestion des devises
 * Solution simplifiée qui détermine la devise sans API externe
 */
public class CurrencyService {

    // Devise de base (dinar tunisien)
    private static final String BASE_CURRENCY = "TND";

    // Taux de change manuels par rapport au dinar tunisien (TND)
    // Ces valeurs sont approximatives et doivent être mises à jour régulièrement
    private static final Map<String, Double> EXCHANGE_RATES = new HashMap<>();
    static {
        // 1 unité de devise étrangère = X dinars tunisiens
        EXCHANGE_RATES.put("TND", 1.0);      // Dinar tunisien
        EXCHANGE_RATES.put("EUR", 3.3);      // Euro
        EXCHANGE_RATES.put("USD", 3.1);      // Dollar américain
        EXCHANGE_RATES.put("GBP", 3.9);      // Livre sterling
        EXCHANGE_RATES.put("CAD", 2.25);     // Dollar canadien
        EXCHANGE_RATES.put("MAD", 0.31);     // Dirham marocain
        EXCHANGE_RATES.put("DZD", 0.023);    // Dinar algérien
        EXCHANGE_RATES.put("EGP", 0.1);      // Livre égyptienne
        EXCHANGE_RATES.put("SAR", 0.83);     // Riyal saoudien
        EXCHANGE_RATES.put("AED", 0.84);     // Dirham des Émirats
        // Ajoutez d'autres devises selon vos besoins
    }

    // Correspondance entre préfixes d'adresses IP et pays/devises
    private static final Map<String, String> IP_COUNTRY_MAP = new HashMap<>();
    static {
        // Tunisie
        IP_COUNTRY_MAP.put("197.0.", "TND");    // Tunisie
        IP_COUNTRY_MAP.put("197.1.", "TND");    // Tunisie
        IP_COUNTRY_MAP.put("197.2.", "TND");    // Tunisie
        IP_COUNTRY_MAP.put("197.3.", "TND");    // Tunisie
        IP_COUNTRY_MAP.put("41.226.", "TND");   // Tunisie
        IP_COUNTRY_MAP.put("41.227.", "TND");   // Tunisie

        // France (EUR)
        IP_COUNTRY_MAP.put("2.0.", "EUR");      // France
        IP_COUNTRY_MAP.put("2.1.", "EUR");      // France
        IP_COUNTRY_MAP.put("2.2.", "EUR");      // France
        IP_COUNTRY_MAP.put("2.3.", "EUR");      // France
        IP_COUNTRY_MAP.put("2.4.", "EUR");      // France
        IP_COUNTRY_MAP.put("212.27.", "EUR");   // France

        // États-Unis (USD)
        IP_COUNTRY_MAP.put("3.0.", "USD");      // USA
        IP_COUNTRY_MAP.put("3.1.", "USD");      // USA
        IP_COUNTRY_MAP.put("4.0.", "USD");      // USA
        IP_COUNTRY_MAP.put("8.0.", "USD");      // USA

        // Royaume-Uni (GBP)
        IP_COUNTRY_MAP.put("2.16.", "GBP");     // UK
        IP_COUNTRY_MAP.put("2.17.", "GBP");     // UK
        IP_COUNTRY_MAP.put("2.18.", "GBP");     // UK

        // Autres pays européens (EUR)
        IP_COUNTRY_MAP.put("5.0.", "EUR");      // Europe
        IP_COUNTRY_MAP.put("5.1.", "EUR");      // Europe

        // Maroc (MAD)
        IP_COUNTRY_MAP.put("105.128.", "MAD");  // Maroc
        IP_COUNTRY_MAP.put("105.129.", "MAD");  // Maroc
        IP_COUNTRY_MAP.put("160.176.", "MAD");  // Maroc

        // Algérie (DZD)
        IP_COUNTRY_MAP.put("41.96.", "DZD");    // Algérie
        IP_COUNTRY_MAP.put("41.97.", "DZD");    // Algérie
        IP_COUNTRY_MAP.put("41.98.", "DZD");    // Algérie
    }

    /**
     * Détecte l'adresse IP de l'utilisateur
     * @return l'adresse IP sous forme de chaîne
     */
    public static String detectUserIP() {
        try {
            // Méthode pour obtenir l'adresse IP publique
            URL url = new URL("https://api.ipify.org");
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String ip = reader.readLine();
            reader.close();
            System.out.println("IP détectée: " + ip);
            return ip;
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback: essayer de détecter l'IP locale
            try {
                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements()) {
                    NetworkInterface networkInterface = interfaces.nextElement();
                    if (networkInterface.isUp() && !networkInterface.isLoopback()) {
                        Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                        while (addresses.hasMoreElements()) {
                            InetAddress addr = addresses.nextElement();
                            if (addr.getHostAddress().contains(".") && !addr.isLoopbackAddress()) {
                                System.out.println("IP locale détectée: " + addr.getHostAddress());
                                return addr.getHostAddress();
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return "127.0.0.1"; // Valeur par défaut si impossible de détecter
        }
    }

    /**
     * Détermine la devise en fonction de l'adresse IP sans utiliser d'API externe
     * @param ipAddress l'adresse IP à analyser
     * @return le code de devise (EUR, USD, etc.)
     */
    public static String getCurrencyFromIP(String ipAddress) {
        try {
            System.out.println("Analyse de l'IP: " + ipAddress);

            // Vérifier si l'adresse IP correspond à l'un des préfixes connus
            for (Map.Entry<String, String> entry : IP_COUNTRY_MAP.entrySet()) {
                String prefix = entry.getKey();
                if (ipAddress.startsWith(prefix)) {
                    String currency = entry.getValue();
                    System.out.println("Devise détectée: " + currency);
                    return currency;
                }
            }

            // Si l'adresse IP ne correspond à aucun préfixe connu,
            // on peut essayer d'analyser les deux premiers octets
            String[] octets = ipAddress.split("\\.");
            if (octets.length >= 2) {
                String firstTwoOctets = octets[0] + "." + octets[1] + ".";
                for (Map.Entry<String, String> entry : IP_COUNTRY_MAP.entrySet()) {
                    String prefix = entry.getKey();
                    if (firstTwoOctets.startsWith(prefix)) {
                        String currency = entry.getValue();
                        System.out.println("Devise détectée par préfixe partiel: " + currency);
                        return currency;
                    }
                }
            }

            // Si on ne peut pas déterminer la devise, on utilise le dinar tunisien par défaut
            System.out.println("Aucune correspondance trouvée, utilisation de la devise par défaut: " + BASE_CURRENCY);
            return BASE_CURRENCY;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur lors de la détection de devise, utilisation de la devise par défaut: " + BASE_CURRENCY);
            return BASE_CURRENCY;
        }
    }

    /**
     * Obtient le taux de change entre une devise source et le dinar tunisien
     * @param sourceCurrency code de la devise source (EUR, USD, etc.)
     * @return taux de conversion vers le dinar tunisien
     */
    public static double getExchangeRate(String sourceCurrency) {
        // Si la devise source est déjà le dinar tunisien, le taux est 1
        if (sourceCurrency.equals(BASE_CURRENCY)) {
            return 1.0;
        }

        // Récupérer le taux de change depuis notre map prédéfinie
        Double rate = EXCHANGE_RATES.get(sourceCurrency);
        if (rate != null) {
            System.out.println("Taux de change " + sourceCurrency + " vers TND: " + rate);
            return rate;
        }

        // Si la devise n'est pas dans notre liste, on utilise 1 par défaut
        System.out.println("Taux de change non trouvé pour " + sourceCurrency + ", utilisation de 1.0");
        return 1.0;
    }

    /**
     * Convertit un prix d'une devise source vers le dinar tunisien
     * @param price prix dans la devise source
     * @param sourceCurrency code de la devise source
     * @return prix en dinar tunisien
     */
    public static double convertToTND(double price, String sourceCurrency) {
        double exchangeRate = getExchangeRate(sourceCurrency);
        double convertedPrice = price * exchangeRate;
        System.out.println("Conversion: " + price + " " + sourceCurrency +
                " = " + convertedPrice + " TND (taux: " + exchangeRate + ")");
        return convertedPrice;
    }
}