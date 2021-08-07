package com.assessbyphone.zambia.CallbackUtils;

import android.content.Context;

public class Defaults {

    public static final String APPLICATION_ID = "68ADC96B-BDCA-DB5A-FFF4-FF76F3849100";
    public static final String SECRET_KEY = "AB1B4854-1F02-373C-FF6E-C053B355A700";
    public static final String SERVER_URL = "https://api.backendless.com";

    public static COUNTRY getCountry(Context context) {
        // use "..."+"last letter" string concat format to avoid rename refactor messing up countries
        if (context.getPackageName().endsWith("chin" + "a"))
            return COUNTRY.CHINA;
        else if (context.getPackageName().endsWith("colombi" + "a"))
            return COUNTRY.COLOMBIA;
        else if (context.getPackageName().endsWith("gambi" + "a"))
            return COUNTRY.GAMBIA;
        else if (context.getPackageName().endsWith("ghan" + "a"))
            return COUNTRY.GHANA;
        else if (context.getPackageName().endsWith("fij" + "i"))
            return COUNTRY.FIJI;
        else if (context.getPackageName().endsWith("liberi" + "a"))
            return COUNTRY.LIBERIA;
        else if (context.getPackageName().endsWith("rwand" + "a"))
            return COUNTRY.RWANDA;
        else if (context.getPackageName().endsWith("samo" + "a"))
            return COUNTRY.SAMOA;
        else if (context.getPackageName().endsWith("tanzani" + "a"))
            return COUNTRY.TANZANIA;
        else if (context.getPackageName().endsWith("ugand" + "a"))
            return COUNTRY.UGANDA;
        else if (context.getPackageName().endsWith("zambi" + "a"))
            return COUNTRY.ZAMBIA;
        return null;
    }

    public static String getBackendlessAppId(Context context) {
        switch (getCountry(context)) {
            case CHINA:
                return "6435193A-3D7D-4945-FF97-B710BF8B2700";
            case COLOMBIA:
                return "2DE14B41-28C9-88D7-FF3C-3E3C8A882500";
            case GAMBIA:
                return "1AADB61C-5CEC-65D6-FF9F-B027A1FCC100";
            case GHANA:
                return "BD0781FE-AEF5-99A8-FF85-80DFCC400700";
            case FIJI:
                return "78F75A56-2A68-C11B-FFD7-2EAB41C0CB00";
            case LIBERIA:
                return "D50FBD57-A6E7-F536-FFED-F06806094F00";
            case RWANDA:
                return "47FBAFB6-63C8-7E83-FF08-F3CEC453F700";
            case SAMOA:
                return "9F08D43B-00F9-C594-FF61-E8E42B189E00";
            case TANZANIA:
                return "0B563DF6-8A81-1C38-FF87-51E0DA286300";
            case UGANDA:
                return "AE7C1C6A-189B-7712-FF29-2CC2D74C1600";
            case ZAMBIA:
                return "B7C4CBBF-78C6-D9CB-FF28-916B08C4C200";
        }
        return null;
    }

    public static String getBackendlessAppKey(Context context) {
        switch (getCountry(context)) {
            case CHINA:
                return "B5232629-8EF6-D56A-FF3E-9C78C75ED200";
            case COLOMBIA:
                return "AB1B4854-1F02-373C-FF6E-C053B355A700";
            case GAMBIA:
                return "AB1B4854-1F02-373C-FF6E-C053B355A700";
            case GHANA:
                return "AB1B4854-1F02-373C-FF6E-C053B355A700";
            case FIJI:
                return "DC6433B8-6FE0-BA89-FF04-6D8A73FBF300";
            case LIBERIA:
                return "483FA32E-782E-DD16-FFA3-FCFDA5A8C500";
            case RWANDA:
                return "AB1B4854-1F02-373C-FF6E-C053B355A700";
            case SAMOA:
                return "C07EC7F8-E05C-16AA-FFAB-75BC7BEC2B00";
            case TANZANIA:
                return "AB1B4854-1F02-373C-FF6E-C053B355A700";
            case UGANDA:
                return "AB1B4854-1F02-373C-FF6E-C053B355A700";
            case ZAMBIA:
                return "A63D7A66-4FDB-7F93-FFA8-F3A8C4B38D00";
        }
        return null;
    }

    public static String getBaseUrl(Context context) {
        switch (getCountry(context)) {
            case CHINA:
                return "http://www.phonicschina.net/pbp/";
            case COLOMBIA:
                return "http://www.phonicscolombia.net/pbp/";
            case GAMBIA:
                return "http://www.phonicsgambia.net/pbp/";
            case GHANA:
                return "http://www.phonicsghana.net/pbp/";
            case FIJI:
                return "http://www.phonicsfiji.net/pbp/";
            case LIBERIA:
                return "http://www.phonicszambia.net/pbp/liberia/";
            case RWANDA:
                return "http://www.phonicsrwanda.net/pbp/";
            case SAMOA:
                return "http://www.phonicssamoa.net/pbp/";
            case TANZANIA:
                return "http://www.phonicstanzania.net/pbp/";
            case UGANDA:
                return "http://www.phonicsuganda.net/pbp/";
            case ZAMBIA:
                return "http://www.phonicszambia.net/pbp/";
        }
        return null;
    }

    public enum COUNTRY {
        CHINA,
        COLOMBIA,
        GAMBIA,
        GHANA,
        FIJI,
        LIBERIA,
        RWANDA,
        SAMOA,
        TANZANIA,
        UGANDA,
        ZAMBIA
    }
}
