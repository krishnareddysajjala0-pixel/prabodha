package com.igv.bookstall;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.*;
import android.text.*;
import android.view.*;
import android.widget.*;
import androidx.core.content.FileProvider;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

@SuppressWarnings({"SetTextI18n", "HardcodedText", "deprecation", "ResultOfMethodCallIgnored"})
public class MainActivity extends Activity {
    static class Book {
        int no, price, qty;
        String name;
        Book(int n, String s, int p) { no = n; name = s; price = p; }
    }

    Map<String, ArrayList<Book>> langMap = new HashMap<>();
    LinearLayout list;
    TextView totalText;
    EditText search;
    int purple = Color.rgb(94, 53, 177), green = Color.rgb(21, 148, 71);

    int selectedLangIndex = 0;
    String selectedLanguage = "తెలుగు";
    String[] languages = {"తెలుగు", "కన్నడ", "ଓଡ଼ିଆ", "\u200Eاُردُو", "हिंदी"};
    String[] langKeys = {"TE", "KN", "OD", "UR", "HI"};
    String[] pdfLangTitles = {"తెలుగు గ్రంథములు", "కన్నడ గ్రంథములు", "ఒడిశా గ్రంథములు", "ఉర్దూ గ్రంథములు", "హిందీ గ్రంథములు"};
    int[] langColors = {
        Color.rgb(94, 53, 177),   // Telugu: Deep Purple
        Color.rgb(21, 101, 192),  // Kannada: Deep Royal Blue (Non-Red)
        Color.rgb(0, 121, 107),   // Odia: Deep Teal
        Color.rgb(194, 24, 91),   // Urdu: Deep Magenta
        Color.rgb(230, 81, 0)     // Hindi: Deep Amber
    };

    static String cleanLangKey(String s) {
        if (s == null) return "";
        return s.replace("\u200E", "").replace("\u200F", "").trim();
    }

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().build());
        int navyBlue = Color.rgb(18, 59, 104);

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(navyBlue);
        getWindow().setNavigationBarColor(navyBlue);

        View decor = getWindow().getDecorView();
        decor.setBackgroundColor(navyBlue);

        int flags = decor.getSystemUiVisibility();
        flags &= ~View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        }
        decor.setSystemUiVisibility(flags);

        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        loadBooks();
        buildUi();
    }

    int getLanguageColor() {
        if (selectedLangIndex >= 0 && selectedLangIndex < langColors.length) {
            return langColors[selectedLangIndex];
        }
        return Color.rgb(20, 35, 60);
    }

    String getPdfLangTitle(String lang) {
        String l = cleanLangKey(lang);
        return switch (l) {
            case "తెలుగు" -> "తెలుగు గ్రంథములు";
            case "కన్నడ" -> "కన్నడ గ్రంథములు";
            case "ଓଡ଼ిଆ", "ఒడిశా", "ఒరియా" -> "ఒడిశా గ్రంథములు";
            case "اُردُو", "ఉర్దూ" -> "ఉర్దూ గ్రంథములు";
            case "हिंदी", "హిందీ" -> "హిందీ గ్రంథములు";
            default -> l + " గ్రంథములు";
        };
    }

    @SuppressWarnings("SimplifiableStringNormalization")
    static String transliterateToEnglish(String teluguText) {
        if (teluguText == null) return "";
        StringBuilder sb = new StringBuilder();
        int len = teluguText.length();
        for (int i = 0; i < len; i++) {
            char c = teluguText.charAt(i);
            switch (c) {
                case 'అ': sb.append("a"); break;
                case 'ఆ': sb.append("aa"); break;
                case 'ఇ': case 'ి': sb.append("i"); break;
                case 'ఈ': case 'ీ': sb.append("ee"); break;
                case 'ఉ': case 'ు': sb.append("u"); break;
                case 'ఊ': case 'ూ': sb.append("oo"); break;
                case 'ఋ': case 'ృ': sb.append("ru"); break;
                case 'ఎ': case 'ె': sb.append("e"); break;
                case 'ఏ': case 'ే': sb.append("ae"); break;
                case 'ఐ': case 'ై': sb.append("ai"); break;
                case 'ఒ': case 'ొ': sb.append("o"); break;
                case 'ఓ': case 'ో': sb.append("oo"); break;
                case 'ఔ': case 'ౌ': sb.append("au"); break;

                case 'క': sb.append("ka"); break;
                case 'ఖ': sb.append("kha"); break;
                case 'గ': sb.append("ga"); break;
                case 'ఘ': sb.append("gha"); break;
                case 'చ': sb.append("cha"); break;
                case 'ఛ': sb.append("chha"); break;
                case 'జ': sb.append("ja"); break;
                case 'ఝ': sb.append("jha"); break;
                case 'ట': sb.append("ta"); break;
                case 'ఠ': sb.append("tha"); break;
                case 'డ': sb.append("da"); break;
                case 'ఢ': sb.append("dha"); break;
                case 'ణ': sb.append("na"); break;
                case 'త': sb.append("ta"); break;
                case 'థ': sb.append("tha"); break;
                case 'ద': sb.append("da"); break;
                case 'ధ': sb.append("dha"); break;
                case 'న': sb.append("na"); break;
                case 'ప': sb.append("pa"); break;
                case 'ఫ': sb.append("pha"); break;
                case 'బ': sb.append("ba"); break;
                case 'భ': sb.append("bha"); break;
                case 'మ': sb.append("ma"); break;
                case 'య': sb.append("ya"); break;
                case 'ర': sb.append("ra"); break;
                case 'ల': sb.append("la"); break;
                case 'వ': sb.append("va"); break;
                case 'శ': sb.append("sha"); break;
                case 'ష': sb.append("sha"); break;
                case 'స': sb.append("sa"); break;
                case 'హ': sb.append("ha"); break;
                case 'ళ': sb.append("la"); break;
                case 'ా': break;

                case 'ం': sb.append("m"); break;
                case '్':
                    int sbLen = sb.length();
                    if (sbLen > 0 && sb.charAt(sbLen - 1) == 'a') {
                        sb.deleteCharAt(sbLen - 1);
                    }
                    break;
                default:
                    if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == ' ') {
                        sb.append(Character.toLowerCase(c));
                    }
                    break;
            }
        }
        return sb.toString();
    }

    void sortBooksAlphabetically(ArrayList<Book> bookList) {
        Collections.sort(bookList, (b1, b2) -> {
            // Rule 1: Books with price <= 25 (e.g. 25 Rupees books) are arranged at the very end
            boolean b1Is25 = (b1.price <= 25);
            boolean b2Is25 = (b2.price <= 25);
            if (b1Is25 != b2Is25) {
                return b1Is25 ? 1 : -1;
            }

            // Rule 2: Transliterate Telugu pronunciation into English letters A-Z for sorting
            String t1 = transliterateToEnglish(b1.name);
            String t2 = transliterateToEnglish(b2.name);
            return t1.compareTo(t2);
        });

        for (int i = 0; i < bookList.size(); i++) {
            bookList.get(i).no = i + 1;
        }
    }

    void loadBooks() {
        // 1. Telugu (103 Books)
        String[][] teluguData = {
            {"త్రైత సిద్ధాంత భగవద్గీత","500"},{"ఇందూసాంప్రదాయాలు","120"},{"ఇందుత్వమును కాపాడుదాం","50"},{"ఆధ్యాత్మిక ప్రశ్నలు - జవాబులు","400"},{"హిందూమతములో కులవివక్ష","75"},{"తిట్ల జ్ఞానము- దీవెనల అజ్ఞానము","60"},{"శ్రీ కృష్ణుడు దేవుడా! భగవంతుడా!!","250"},{"యజ్ఞములు (నిజమా-అబద్దమా?)","70"},{"ఏ మతములో ఎంత మతద్వేషము?","65"},{"హిందూమతములో సిద్ధాంతకర్తలు","210"},{"దైవగ్రంథములో సత్యాసత్య విచక్షణ","50"},{"దేవుని రాకకు ఇది సమయముకాదా!","60"},{"మూడు గ్రంథములు, ఇద్దరు గురువులు","65"},{"దయ్యాల-భూతాల యథార్థ సంఘటనలు","300"},{"ప్రతిమ X విగ్రహ - దైవము X దైయ్యము","120"},{"త్రైతాకార రహస్యము(త్రైతాకార బెర్ముడా )","130"},{"హేతువాద ప్రశ్నలు-సత్యవాద జవాబులు","115"},{"జ్యోతిష్య శాస్త్రం","280"},
            {"మంత్రము-మహిమ (నిజమా-అబద్దమా?)","60"},{"అంతిమ దైవగ్రంథములో జ్ఞానవాక్యములు","500"},{"మన పండుగలు (ఎలా చేయాలో తెలుసా?)","100"},{"కలియుగము(ఎప్పటికీయుగాంతముకాదు)","50"},{"కృష్ణమూస (శ్రీ కృష్ణ మరణము తర్వాత జీవితము)","60"},{"స్వర్గము ఇంద్రలోకమా! నరకము యమరాజ్యమా!!","80"},{"మూడు దైవ గ్రంథములు-మూడు ప్రథమ వాక్యములు","60"},{"తీర్పు-దేవుని తీర్పు","70"},{"గుత్తా","50"},{"ప్రబోధ","200"},{"సుబోధ","230"},{"ఆదిత్య","60"},{"సమాధి","90"},{"తల్లి - తండ్రి","70"},{"గీటురాయి","140"},{"త్రైతారాధన","70"},{"ధర్మచక్రము","120"},{"ప్రసిద్ధి బోధ","500"},{"కర్మ పత్రము","70"},{"నీకు నా లేఖ","60"},{"ఒక్కడే ఇద్దరు","90"},{"దేవునిముద్ర","110"},{"భావము-భాష","70"},{"కథల జ్ఞానము","140"},{"సత్యాన్వేషి కథ","220"},{"సిలువ దేవుడా?","50"},{"ఆత్మలింగార్థము","110"},{"ప్రాథమిక జ్ఞానము","65"},
            {"విశ్వ విద్యాలయము","70"},{"జీహాద్ అంటే యుద్దమా?","55"},{"జనన మరణ సిద్ధాంతము","100"},{"మతాతీత దేవుని మార్గము","90"},
            {"దేవుని చిహ్నము","70"},{"మతము-పథము","45"},{"ద్రావిడ బ్రాహ్మణ","100"},{"ప్రవక్తలు ఎవరు?","50"},{"ధర్మశాస్త్రము ఏది?","60"},{"దేవుని గుర్తు- 963","70"},{"ఇందువు క్రైస్తవుడా?","70"},{"నాస్తికులు-ఆస్తికులు","50"},{"ప్రబోధ తరంగాలు","100"},{"గీతా పరిచయము","70"},{"త్రైత సిద్ధాంతము","70"},{"వార్తకుడు-వర్తకుడు","60"},{"ధర్మము-అధర్మము","100"},{"తత్వముల జ్ఞానము","90"},{"మరణ రహస్యము","80"},{"పునర్జన్మ రహస్యము","110"},{"గురు ప్రార్థనార్థ మంజరి","110"},{"సామెతల జ్ఞానము","60"},
            {"పొడుపు కథల జ్ఞానము","100"},{"ఏది నిజమైన జ్ఞానము?","100"},{"నిగూఢ తత్వార్థ బోధిని","130"},{"దేవాలయ రహస్యములు","150"},{"ప్రబోధానందం నాటికలు","70"},{"హేతువాదము-ప్రతివాదము","220"},{"మతమార్పిడి దైవ ద్రోహము","90"},{"నాది ఆలోచన - నీది ఆలోచన","50"},{"ధ్యానము-ప్రార్ధన -నమాజ్","65"},{"సాయిబాబా దేవుడా! కాదా?","75"},{"లు అంటే ఏమిటి ? (తెలుగు)","60"},{"చెట్టుముందా! విత్తుముందా?","60"},{"గీతం గీత (పాటల జ్ఞానము)","200"},{"మరణము తర్వాత జీవితము","65"},{"ప్రథమ దైవ గ్రంథము భగవద్గీత","130"},{"దేవుని జ్ఞానము కబ్జా అయ్యింది","50"},{"అజ్ఞానముతో ఉగ్రవాద బీజాలు","80"},{"ఒక మాట మూడు గ్రంథములు","50"},{"త్రైత సిద్ధాంత ఆధ్యాత్మిక ఘంటు","90"},{"ఏసు చనిపోయాడా? చంపబడ్డాడా ?","60"},{"ఏది సత్యము-ఏది అసత్యము","70"},{"ఒక వ్యక్తి రెండు కోణములు","70"},{"అంతిమ దైవ గ్రంథములో వజ్ర వాక్యములు","515"},{"బ్రహ్మ-రావణబ్రహ్మ- భగవాన్ రావణ బ్రహ్మ","90"},{"ద్వితీయ దైవగ్రంథములో రత్న వాక్యములు","280"},{"హిందూధర్మమునకు రక్షణ అవసరమా?","75"},{"వేదములు మనిషికి అవసరమా?","80"},{"ఉపనిషత్తులలో లోపాలు","80"},
            {"ఖురాన్ హదీస్ ఏది ముఖ్యము?","70"},{"భక్తిలో మీరు సంసారులా? వ్యభిచారులా?","90"},{"సుప్రసిద్ధి బోధ","700"},{"శతము100","80"},
            {"సిద్ధిబోధ","500"},{"గీతా రత్నములు","25"},{"వేమన రత్నములు","25"}
        };
        ArrayList<Book> listTe = new ArrayList<>();
        int n = 1;
        for (String[] x : teluguData) listTe.add(new Book(n++, x[0], Integer.parseInt(x[1])));
        sortBooksAlphabetically(listTe);
        langMap.put("TE", listTe);

        // 2. Kannada (94 Books)
        String[][] kannadaData = {
            {"ఆదిత్య","60"},{"ఆధ్యాత్మిక ప్రశ్నలు జవాబులు","380"},{"అజ్ఞానంలో ఉగ్రవాద బీజాలు","110"},{"ఆత్మలింగార్థం","115"},{"భగవాన్ రావణబ్రహ్మ","80"},{"భక్తిలో సంసారుల వ్యభిచారుల","100"},{"భావము భాష","70"},{"చెట్టు ముందా విత్తు ముందా","45"},{"దయ్యాల భూతాల యదార్థ సంఘటనలు","270"},{"ధర్మము ఆ ధర్మము","70"},{"ధర్మశాస్త్రము ఏది?","50"},{"దేవుని చిహ్నము","80"},{"దేవుని ముద్ర","120"},{"దేవుని గుర్తు 963","70"},{"దేవుని జ్ఞానం కబ్జా అయింది","60"},{"దేవుని రాకకు ఇది సమయం కాదా","60"},{"దేవాలయ రహస్యములు","120"},{"దైవ గ్రంథములో సత్యాసత్య విచక్షణ","50"},{"ద్వితీయ దైవ గ్రంథంలో రత్న వాక్యములు","300"},{"ధ్యానము నమాజ్ ప్రార్ధన","55"},{"ద్రావిడ బ్రాహ్మణ","110"},{"ధర్మ చక్రం","125"},{"ఏది నిజమైన జ్ఞానము","85"},{"ఏది సత్యం ఏది అసత్యం","70"},{"ఏ మతంలో ఎంత మతద్వేషము","80"},{"యేసు చనిపోయాడా చంపబడ్డాడా","45"},{"గీతా పరిచయం","70"},{"గీతా రత్నములు","25"},{"గీటు రాయి","170"},{"గుత్తా","45"},{"గురువు","90"},{"హేతువాద ప్రశ్నలు సత్యవాద జవాబులు","135"},{"హేతువాదం ప్రతివాదం","280"},{"హిందూమతంలో కులవివక్ష","65"},{"హిందూ ధర్మమునకు రక్షణ అవసరమా","75"},{"హిందూమతంలో సిద్ధాంతకర్తలు","210"},{"హిందుత్వమును కాపాడుదాం","70"},{"హిందూ క్రైస్తవుడా","100"},{"హిందూ సాంప్రదాయంలు","120"},{"జనన మరణ సిద్ధాంతం","80"},{"జిహాద్ అంటే యుద్ధమా?","70"},{"జ్యోతిష్య శాస్త్రము","300"},{"కలియుగము","80"},{"కర్మ పత్రము","65"},{"కథల జ్ఞానము","100"},{"కృష్ణ మూస","65"},{"లు అంటే ఏమిటి","45"},{"మన పండుగలు","90"},{"మంత్రము మహిమ","50"},{"మతము పథము","45"},{"మతమార్పిడి దైవ ద్రోహం","70"},{"మతాతీత దేవుని మార్గము","90"},{"మరణం తర్వాత జీవితం","50"},{"మరణ రహస్యం","70"},{"మూడు దైవ గ్రంథంలో మూడు ప్రథమ వాక్యములు","60"},{"మూడు గ్రంథములు ఇద్దరు గురువులు ఒక బోధకుడు","90"},{"నీకు నా లేఖ","60"},{"నాస్తికులు ఆస్తికులు","45"},{"నిగూఢ తత్వార్థ బోధిని","115"},{"ఒక్కడే ఇద్దరు","90"},{"ఒక మాట మూడు గ్రంథములు","45"},{"ఒక వ్యక్తి రెండు కోణములు","80"},{"ప్రబోధానందం నాటికలు","60"},{"ప్రబోధ తరంగాలు","70"},{"ప్రబోధ","200"},{"ప్రథమ దైవ గ్రంథం భగవద్గీత","110"},{"ప్రతిమ విగ్రహ","100"},{"ప్రాథమిక జ్ఞానం","65"},{"పొడుపు కథల జ్ఞానం","90"},{"పునర్జన్మ రహస్యం","80"},{"ప్రవక్తలు ఎవరు","45"},{"ఖురాన్ హదీసులు ఏది ముఖ్యం","90"},{"సాయిబాబా దేవుడా కాదా","70"},{"సమాధి","70"},{"సామెతల జ్ఞానము","85"},{"శతము","90"},{"శిలువ దేవుడా","45"},{"సత్యాన్వేషి కథ","380"},{"శ్రీకృష్ణుడు దేవుడా భగవంతుడా","250"},{"సుబోధ","180"},{"స్వర్గము ఇంద్రలోకమా నరకము యమ రాజ్యమా","110"},{"తల్లి తండ్రి","60"},{"తీర్పు","70"},{"తిట్ల జ్ఞానము దీవెనల అజ్ఞానము","50"},{"తత్వముల వివరము","75"},{"త్రైత సిద్ధాంతము","110"},{"త్రైత సిద్ధాంత భగవద్గీత","500"},{"త్రైతాకార రహస్యము","90"},{"త్రైతారాధన","60"},{"ఉపనిషత్తులలో లోపములు","90"},{"వార్తకుడు వర్తకుడు","45"},{"విశ్వవిద్యాలయం","50"},{"వేదములు మనిషికి అవసరమా","75"},{"యజ్ఞములు","50"}
        };
        ArrayList<Book> listKn = new ArrayList<>();
        n = 1;
        for (String[] x : kannadaData) listKn.add(new Book(n++, x[0], Integer.parseInt(x[1])));
        sortBooksAlphabetically(listKn);
        langMap.put("KN", listKn);

        // 3. Odia (13 Books)
        String[][] odiaData = {
            {"మంత్రము మహిమ","60"},{"గీతా పరిచయం","70"},{"పునర్జన్మ రహస్యం","110"},{"మరణ రహస్యం","90"},{"మన పండుగలు","100"},{"యజ్ఞములు","60"},{"దేవాలయ రహస్యములు","160"},{"ప్రబోధ","200"},{"గురు ప్రార్థనా మంజరి","110"},{"భగవద్గీత","500"},{"ప్రబోధ తరంగమూ","100"},{"జనన మరణ సిద్ధాంతం","100"},{"తల్లిదండ్రి","70"}
        };
        ArrayList<Book> listOd = new ArrayList<>();
        n = 1;
        for (String[] x : odiaData) listOd.add(new Book(n++, x[0], Integer.parseInt(x[1])));
        sortBooksAlphabetically(listOd);
        langMap.put("OD", listOd);

        // 4. Urdu (9 Books)
        String[][] urduData = {
            {"ఏది సత్యం ఏది అసత్యం","70"},{"దేవుని చిహ్నం","85"},{"సమాధి","60"},{"జ్ఞాన రత్నములు","40"},{"దేవుని ముద్ర","105"},{"గీటు రాయి","120"},{"ధర్మచక్రం","125"},{"అంతిమ దైవ గ్రంథం జ్ఞాన వాక్యములు","500"},{"అంతిమ దైవ గ్రంధం వజ్రవాక్యములు","500"}
        };
        ArrayList<Book> listUr = new ArrayList<>();
        n = 1;
        for (String[] x : urduData) listUr.add(new Book(n++, x[0], Integer.parseInt(x[1])));
        sortBooksAlphabetically(listUr);
        langMap.put("UR", listUr);

        // 5. Hindi (4 Books)
        String[][] hindiData = {
            {"మన పండుగలు","90"},{"ప్రబోధ","200"},{"ఇందూసాంప్రదాయాలు","100"},{"దేవాలయ రహస్యములు","115"}
        };
        ArrayList<Book> listHi = new ArrayList<>();
        n = 1;
        for (String[] x : hindiData) listHi.add(new Book(n++, x[0], Integer.parseInt(x[1])));
        sortBooksAlphabetically(listHi);
        langMap.put("HI", listHi);
    }

    ArrayList<Book> getBooksForCurrentLang() {
        if (selectedLangIndex < 0 || selectedLangIndex >= langKeys.length) {
            selectedLangIndex = 0;
        }
        ArrayList<Book> l = langMap.get(langKeys[selectedLangIndex]);
        if (l == null) l = langMap.get("TE");
        return l;
    }

    TextView tv(String s, int c) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(13);
        t.setTextColor(c);
        t.setGravity(Gravity.CENTER_VERTICAL);
        t.setPadding(8, 6, 8, 6);
        return t;
    }

    GradientDrawable bg(int color, float r) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(r);
        return g;
    }

    void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(244, 247, 253));

        LinearLayout headerBox = new LinearLayout(this);
        headerBox.setOrientation(LinearLayout.VERTICAL);
        headerBox.setGravity(Gravity.CENTER);
        headerBox.setPadding(dp(12), dp(8), dp(12), dp(8));
        headerBox.setBackgroundColor(Color.rgb(18, 59, 104));

        LinearLayout headerRow = new LinearLayout(this);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(Gravity.CENTER);

        ImageView iconLeft = new ImageView(this);
        iconLeft.setImageResource(R.mipmap.ic_launcher);
        LinearLayout.LayoutParams lpLeft = new LinearLayout.LayoutParams(dp(28), dp(28));
        lpLeft.setMargins(0, 0, dp(8), 0);

        TextView h1 = new TextView(this);
        h1.setText("ఇందూ జ్ఞానవేదిక ప్రచురణలు");
        h1.setTextSize(19);
        h1.setTypeface(null, Typeface.BOLD);
        h1.setTextColor(Color.rgb(255, 215, 0));
        h1.setGravity(Gravity.CENTER);

        ImageView iconRight = new ImageView(this);
        iconRight.setImageResource(R.mipmap.ic_launcher);
        LinearLayout.LayoutParams lpRight = new LinearLayout.LayoutParams(dp(28), dp(28));
        lpRight.setMargins(dp(8), 0, 0, 0);

        headerRow.addView(iconLeft, lpLeft);
        headerRow.addView(h1);
        headerRow.addView(iconRight, lpRight);
        headerBox.addView(headerRow);

        TextView hSub1 = new TextView(this);
        hSub1.setText("యోగీశ్వరుల వారి సంచలనాత్మక, త్రిమత ఆధ్యాత్మిక రచనలు");
        hSub1.setTextSize(12.5f);
        hSub1.setTypeface(null, Typeface.BOLD);
        hSub1.setTextColor(Color.WHITE); // Solid White Color
        hSub1.setGravity(Gravity.CENTER);
        hSub1.setPadding(0, dp(2), 0, dp(1));
        headerBox.addView(hSub1);

        TextView hSub2 = new TextView(this);
        hSub2.setText("\"త్రైత సిద్ధాంత ఆధ్యాత్మిక గ్రంథములు మానవ జీవితమునకు గొప్ప వెలుగు\"");
        hSub2.setTextSize(11);
        hSub2.setTypeface(null, Typeface.BOLD);
        hSub2.setTextColor(Color.rgb(255, 235, 59)); // Bright Yellow Color
        hSub2.setGravity(Gravity.CENTER);
        hSub2.setPadding(0, dp(1), 0, dp(1));
        headerBox.addView(hSub2);

        root.addView(headerBox);

        LinearLayout searchRow = new LinearLayout(this);
        searchRow.setPadding(dp(10), dp(8), dp(10), dp(6));
        searchRow.setGravity(Gravity.CENTER_VERTICAL);

        search = new EditText(this);
        search.setHint("గ్రంథం పేరు వెతకండి...");
        search.setTextSize(15);
        search.setSingleLine(true);
        search.setBackground(bg(Color.WHITE, 40));
        search.setPadding(dp(14), dp(8), dp(14), dp(8));
        searchRow.addView(search, new LinearLayout.LayoutParams(0, dp(44), 1f));

        // 5 Languages Selector Button
        Button langBtn = new Button(this);
        langBtn.setText(String.format("భాష: %s ▼", selectedLanguage));
        langBtn.setTextColor(Color.WHITE);
        langBtn.setTextSize(12.5f);
        langBtn.setTypeface(null, Typeface.BOLD);
        langBtn.setBackground(bg(green, 40));
        langBtn.setPadding(dp(8), 0, dp(8), 0);
        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(dp(125), dp(44));
        ap.setMargins(dp(6), 0, 0, 0);
        searchRow.addView(langBtn, ap);
        root.addView(searchRow);

        LinearLayout bar = new LinearLayout(this);
        bar.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        bar.setBackgroundColor(purple);
        String[] h = {"క్ర.సం", "గ్రంథం పేరు", "ధర (₹)", "సంఖ్య", "మొత్తం (₹)"};
        float[] w = {0.10f, 0.38f, 0.14f, 0.22f, 0.16f};
        for (int i = 0; i < h.length; i++) {
            TextView x = tv(h[i], Color.WHITE);
            x.setTypeface(null, Typeface.BOLD);
            x.setGravity(Gravity.CENTER);
            bar.addView(x, new LinearLayout.LayoutParams(0, dp(44), w[i]));
        }
        root.addView(bar);

        ScrollView sv = new ScrollView(this);
        sv.setClipChildren(false);
        sv.setClipToPadding(false);

        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setClipChildren(false);
        list.setClipToPadding(false);

        sv.addView(list);
        root.addView(sv, new LinearLayout.LayoutParams(-1, 0, 1f));

        // Restructured Bottom Bar: Left = Clear, Middle = PDF, Right = Total Amount
        LinearLayout bottom = new LinearLayout(this);
        bottom.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        bottom.setPadding(dp(10), dp(4), dp(10), dp(8));
        bottom.setGravity(Gravity.CENTER_VERTICAL);
        bottom.setBackgroundColor(Color.rgb(244, 247, 253));

        Button clearBtn = new Button(this);
        clearBtn.setText("క్లియర్");
        clearBtn.setTextColor(Color.WHITE);
        clearBtn.setTextSize(12);
        clearBtn.setTypeface(null, Typeface.BOLD);
        clearBtn.setBackground(bg(Color.rgb(220, 38, 38), dp(19)));
        clearBtn.setPadding(0, 0, 0, 0);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(dp(68), dp(38));
        cp.setMargins(0, 0, dp(6), 0);
        bottom.addView(clearBtn, cp);

        Button pdf = new Button(this);
        pdf.setText("PDF తయారు చేయండి");
        pdf.setTextColor(Color.WHITE);
        pdf.setTextSize(12);
        pdf.setTypeface(null, Typeface.BOLD);
        pdf.setBackground(bg(green, dp(19)));
        pdf.setPadding(0, 0, 0, 0);
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(dp(138), dp(38));
        bottom.addView(pdf, pp);

        totalText = new TextView(this);
        totalText.setText("మొత్తం వెల: ₹0.00");
        totalText.setTextSize(13.5f);
        totalText.setTextColor(purple);
        totalText.setTypeface(null, Typeface.BOLD);
        totalText.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, dp(38), 1f);
        tp.setMargins(dp(6), 0, 0, 0);
        bottom.addView(totalText, tp);

        root.addView(bottom);

        // Apply Status Bar Inset Padding to headerBox and Navigation Bar Inset Padding to bottom bar
        root.setOnApplyWindowInsetsListener((v, insets) -> {
            int topInset = insets.getSystemWindowInsetTop();
            int bottomInset = insets.getSystemWindowInsetBottom();
            int leftInset = insets.getSystemWindowInsetLeft();
            int rightInset = insets.getSystemWindowInsetRight();

            headerBox.setPadding(dp(12) + leftInset, dp(8) + topInset, dp(12) + rightInset, dp(8));
            bottom.setPadding(dp(10) + leftInset, dp(4), dp(10) + rightInset, dp(8) + bottomInset);

            return insets.consumeSystemWindowInsets();
        });

        setContentView(root);

        clearBtn.setOnClickListener(v -> {
            for (ArrayList<Book> bList : langMap.values()) {
                for (Book b : bList) {
                    b.qty = 0;
                }
            }
            showSelectedOnly = false;
            render(search.getText().toString());
            Toast.makeText(this, "అన్ని ఎంపికలు క్లియర్ చేయబడ్డాయి (0 కి రీసెట్ ఆయెను)", Toast.LENGTH_SHORT).show();
        });

        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int c, int d) {}
            public void onTextChanged(CharSequence s, int a, int b, int c) { render(s.toString()); }
            public void afterTextChanged(Editable e) {}
        });

        langBtn.setOnClickListener(v -> showLanguageSelector(langBtn));

        pdf.setOnClickListener(v -> onPdfButtonClicked());
        render("");
    }

    void showLanguageSelector(Button langBtn) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(20), dp(16), dp(20), dp(16));

        TextView title = new TextView(this);
        title.setText("భాషను ఎంచుకోండి");
        title.setTextSize(18);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(Color.rgb(18, 59, 104));
        title.setPadding(0, 0, 0, dp(12));
        title.setGravity(Gravity.START);
        layout.addView(title);

        AlertDialog dialog = builder.create();

        for (int i = 0; i < languages.length; i++) {
            final int index = i;
            TextView item = new TextView(this);
            item.setText(languages[i]);
            item.setTextSize(16);
            item.setTypeface(null, Typeface.BOLD);
            item.setTextColor(langColors[i]); // Unique color per language
            item.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            item.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            item.setPadding(dp(16), dp(12), dp(16), dp(12));
            
            // Alternating row background colors for differentiation
            int rowBg = (i % 2 == 0) ? Color.rgb(255, 253, 245) : Color.rgb(231, 243, 255);
            item.setBackground(bg(rowBg, dp(8)));
            item.setClickable(true);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
            lp.setMargins(0, dp(4), 0, dp(4));
            item.setLayoutParams(lp);

            item.setOnClickListener(v -> {
                selectedLangIndex = index;
                selectedLanguage = cleanLangKey(languages[index]);
                langBtn.setText(String.format("భాష: %s ▼", selectedLanguage));
                render(search.getText().toString());
                dialog.dismiss();
                Toast.makeText(this, "ఎంపిక చేసిన భాష: " + selectedLanguage, Toast.LENGTH_SHORT).show();
            });

            layout.addView(item);
        }

        dialog.setView(layout);
        dialog.show();
    }

    boolean showSelectedOnly = false;

    void render(String q) {
        list.removeAllViews();

        ArrayList<Book> currentBooks = getBooksForCurrentLang();
        int langColor = getLanguageColor();

        int i = 0;
        for (Book b : currentBooks) {
            if (showSelectedOnly && b.qty == 0) continue;
            if (!q.trim().isEmpty() && !b.name.contains(q.trim())) continue;
            final Book f = b;

            LinearLayout row = new LinearLayout(this);
            row.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(2), dp(3), dp(2), dp(3));
            row.setClipChildren(false);
            row.setClipToPadding(false);
            row.setBackground(bg(i % 2 == 0 ? Color.rgb(255, 253, 245) : Color.rgb(231, 243, 255), 0));

            TextView no = tv("" + b.no, Color.rgb(20, 35, 60));
            no.setGravity(Gravity.CENTER);

            // Book name rendered in its language's unique text color (Non-red)
            TextView name = tv(b.name, langColor);
            name.setTypeface(null, Typeface.BOLD);

            TextView price = tv("₹" + b.price, purple);
            price.setGravity(Gravity.CENTER);

            // Quantity box with Red (-) button, Editable Number Input, and Green (+) button
            LinearLayout qtyBox = new LinearLayout(this);
            qtyBox.setGravity(Gravity.CENTER);
            qtyBox.setClipChildren(false);

            TextView minus = new TextView(this);
            minus.setText("−");
            minus.setTextSize(16);
            minus.setTypeface(null, Typeface.BOLD);
            minus.setTextColor(Color.WHITE);
            minus.setGravity(Gravity.CENTER);
            minus.setIncludeFontPadding(false);
            minus.setBackground(bg(Color.rgb(220, 38, 38), dp(13)));
            minus.setClickable(true);

            EditText qv = new EditText(this);
            qv.setText(String.format(Locale.US, "%d", b.qty));
            qv.setTextSize(13);
            qv.setTypeface(null, Typeface.BOLD);
            qv.setTextColor(Color.rgb(20, 35, 60));
            qv.setGravity(Gravity.CENTER);
            qv.setInputType(InputType.TYPE_CLASS_NUMBER);
            qv.setSelectAllOnFocus(true);
            qv.setBackground(bg(Color.rgb(240, 243, 248), dp(5)));
            qv.setPadding(dp(2), dp(2), dp(2), dp(2));

            TextView plus = new TextView(this);
            plus.setText("+");
            plus.setTextSize(16);
            plus.setTypeface(null, Typeface.BOLD);
            plus.setTextColor(Color.WHITE);
            plus.setGravity(Gravity.CENTER);
            plus.setIncludeFontPadding(false);
            plus.setBackground(bg(Color.rgb(22, 163, 74), dp(13)));
            plus.setClickable(true);

            LinearLayout.LayoutParams lpBtn = new LinearLayout.LayoutParams(dp(26), dp(26));
            qtyBox.addView(minus, lpBtn);
            qtyBox.addView(qv, new LinearLayout.LayoutParams(dp(34), dp(28)));
            qtyBox.addView(plus, lpBtn);

            TextView amt = tv("₹" + (b.qty * b.price), purple);
            amt.setGravity(Gravity.CENTER);

            float[] w = {0.10f, 0.38f, 0.14f, 0.22f, 0.16f};
            View[] vs = {no, name, price, qtyBox, amt};
            for (int z = 0; z < vs.length; z++) {
                row.addView(vs[z], new LinearLayout.LayoutParams(0, dp(58), w[z]));
            }

            final boolean[] isUpdating = {false};

            qv.addTextChangedListener(new TextWatcher() {
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (isUpdating[0]) return;
                    try {
                        String val = s.toString().trim();
                        int newQty = val.isEmpty() ? 0 : Integer.parseInt(val);
                        if (newQty < 0) newQty = 0;
                        if (newQty > 9999) newQty = 9999;
                        f.qty = newQty;
                        amt.setText("₹" + (f.qty * f.price));
                        updateTotals();
                    } catch (NumberFormatException ignored) {}
                }
                public void afterTextChanged(Editable s) {}
            });

            minus.setOnClickListener(v -> {
                if (f.qty > 0) {
                    f.qty--;
                    isUpdating[0] = true;
                    qv.setText(String.format(Locale.US, "%d", f.qty));
                    isUpdating[0] = false;
                    amt.setText("₹" + (f.qty * f.price));
                    updateTotals();
                    if (showSelectedOnly && f.qty == 0) render(search.getText().toString());
                }
            });

            plus.setOnClickListener(v -> {
                f.qty++;
                isUpdating[0] = true;
                qv.setText(String.format(Locale.US, "%d", f.qty));
                isUpdating[0] = false;
                amt.setText("₹" + (f.qty * f.price));
                updateTotals();
            });

            list.addView(row);
            i++;
        }
        updateTotals();
    }

    void updateTotals() {
        int total = 0;
        for (ArrayList<Book> bList : langMap.values()) {
            for (Book b : bList) {
                if (b.qty > 0) {
                    total += b.qty * b.price;
                }
            }
        }
        totalText.setText("మొత్తం వెల: ₹" + String.format(Locale.US, "%,d", total) + ".00");
    }

    void onPdfButtonClicked() {
        int totalQty = 0;
        for (ArrayList<Book> bList : langMap.values()) {
            for (Book b : bList) {
                if (b.qty > 0) totalQty += b.qty;
            }
        }
        if (totalQty == 0) {
            Toast.makeText(this, "దయచేసి కనీసం ఒక గ్రంథాన్ని ఎంచుకోండి", Toast.LENGTH_SHORT).show();
            return;
        }
        showCustomerInfoDialog();
    }

    void showCustomerInfoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        ScrollView scrollView = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(18), dp(16), dp(18), dp(16));
        scrollView.addView(layout);

        // Section 1: Cart Checkout Title (100% Telugu)
        TextView cartTitle = new TextView(this);
        cartTitle.setText("ఎంపిక చేసిన గ్రంథములు");
        cartTitle.setTextSize(17);
        cartTitle.setTypeface(null, Typeface.BOLD);
        cartTitle.setTextColor(Color.rgb(18, 59, 104));
        cartTitle.setPadding(0, 0, 0, dp(8));
        layout.addView(cartTitle);

        // Cart List Container
        LinearLayout cartContainer = new LinearLayout(this);
        cartContainer.setOrientation(LinearLayout.VERTICAL);
        layout.addView(cartContainer);

        // Cart Total Price Text
        TextView cartTotalTxt = new TextView(this);
        cartTotalTxt.setTextSize(14.5f);
        cartTotalTxt.setTypeface(null, Typeface.BOLD);
        cartTotalTxt.setTextColor(purple);
        cartTotalTxt.setPadding(0, dp(8), 0, dp(12));
        cartTotalTxt.setGravity(Gravity.END);
        layout.addView(cartTotalTxt);

        // Initial Cart Populate
        refreshCartView(cartContainer, cartTotalTxt);

        // Divider Line
        View divider = new View(this);
        divider.setBackgroundColor(Color.rgb(220, 224, 230));
        layout.addView(divider, new LinearLayout.LayoutParams(-1, dp(1)));

        // Section 2: Your Info Title (100% Telugu)
        TextView title = new TextView(this);
        title.setText("మీ వివరములు");
        title.setTextSize(17);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(Color.rgb(18, 59, 104));
        title.setPadding(0, dp(12), 0, dp(8));
        layout.addView(title);

        TextView lblName = new TextView(this);
        lblName.setText("పేరు *");
        lblName.setTextSize(13);
        lblName.setTextColor(Color.rgb(80, 80, 80));
        lblName.setPadding(0, dp(2), 0, dp(2));
        layout.addView(lblName);

        EditText inputName = new EditText(this);
        inputName.setHint("ఉదా: ఎమ్. బాలయ్య బాబు");
        inputName.setSingleLine(true);
        inputName.setBackground(bg(Color.rgb(240, 243, 248), dp(6)));
        inputName.setPadding(dp(12), dp(9), dp(12), dp(9));
        layout.addView(inputName);

        TextView lblCommittee = new TextView(this);
        lblCommittee.setText("కమిటీ *");
        lblCommittee.setTextSize(13);
        lblCommittee.setTextColor(Color.rgb(80, 80, 80));
        lblCommittee.setPadding(0, dp(8), 0, dp(2));
        layout.addView(lblCommittee);

        EditText inputCommittee = new EditText(this);
        inputCommittee.setHint("ఉదా: తాడిపత్రి కమిటీ");
        inputCommittee.setSingleLine(true);
        inputCommittee.setBackground(bg(Color.rgb(240, 243, 248), dp(6)));
        inputCommittee.setPadding(dp(12), dp(9), dp(12), dp(9));
        layout.addView(inputCommittee);

        TextView lblPhone = new TextView(this);
        lblPhone.setText("ఫోన్ నంబరు *");
        lblPhone.setTextSize(13);
        lblPhone.setTextColor(Color.rgb(80, 80, 80));
        lblPhone.setPadding(0, dp(8), 0, dp(2));
        layout.addView(lblPhone);

        EditText inputPhone = new EditText(this);
        inputPhone.setHint("ఉదా: 9160377268");
        inputPhone.setSingleLine(true);
        inputPhone.setInputType(InputType.TYPE_CLASS_PHONE);
        inputPhone.setBackground(bg(Color.rgb(240, 243, 248), dp(6)));
        inputPhone.setPadding(dp(12), dp(9), dp(12), dp(9));
        layout.addView(inputPhone);

        TextView lblAddress = new TextView(this);
        lblAddress.setText("చిరునామా *");
        lblAddress.setTextSize(13);
        lblAddress.setTextColor(Color.rgb(80, 80, 80));
        lblAddress.setPadding(0, dp(8), 0, dp(2));
        layout.addView(lblAddress);

        EditText inputAddress = new EditText(this);
        inputAddress.setHint("ఉదా: తాడిపత్రి");
        inputAddress.setLines(2);
        inputAddress.setBackground(bg(Color.rgb(240, 243, 248), dp(6)));
        inputAddress.setPadding(dp(12), dp(9), dp(12), dp(9));
        layout.addView(inputAddress);

        builder.setView(scrollView);
        builder.setPositiveButton("PDF తయారు చేయండి", null);
        builder.setNegativeButton("రద్దు చేయండి", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = inputName.getText().toString().trim();
            String committee = inputCommittee.getText().toString().trim();
            String phone = inputPhone.getText().toString().trim();
            String address = inputAddress.getText().toString().trim();

            int currentTotalQty = 0;
            for (ArrayList<Book> bList : langMap.values()) {
                for (Book b : bList) { if (b.qty > 0) currentTotalQty += b.qty; }
            }

            if (currentTotalQty == 0) {
                Toast.makeText(this, "దయచేసి కనీసం ఒక గ్రంథాన్ని ఎంచుకోండి", Toast.LENGTH_SHORT).show();
            } else if (name.isEmpty() || committee.isEmpty() || phone.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "దయచేసి అన్ని వివరాలను (పేరు, కమిటీ, ఫోన్ నంబరు, చిరునామా) నమోదు చేయండి", Toast.LENGTH_SHORT).show();
            } else {
                dialog.dismiss();
                makePdf(name, committee, phone, address);
            }
        });
    }

    void refreshCartView(LinearLayout cartContainer, TextView cartTotalTxt) {
        cartContainer.removeAllViews();
        int grandTotal = 0;
        int totalCartCount = 0;

        for (int i = 0; i < langKeys.length; i++) {
            String lang = langKeys[i];
            ArrayList<Book> bList = langMap.get(lang);
            if (bList == null) continue;

            boolean sectionHeaderDrawn = false;
            String langDisplayName = languages[i].replace("\u200E", "").replace("\u200F", "").trim();

            for (Book b : bList) {
                if (b.qty > 0) {
                    totalCartCount++;
                    final Book f = b;
                    int bookTotal = b.qty * b.price;
                    grandTotal += bookTotal;

                    // Language Section Banner Header in Cart Popup
                    if (!sectionHeaderDrawn) {
                        TextView langBanner = new TextView(this);
                        langBanner.setText(String.format("భాష: %s", langDisplayName));
                        langBanner.setTextSize(13);
                        langBanner.setTypeface(null, Typeface.BOLD);
                        langBanner.setTextColor(Color.rgb(255, 215, 0)); // Bright Yellow text
                        langBanner.setGravity(Gravity.CENTER); // Central alignment
                        langBanner.setBackground(bg(Color.rgb(18, 59, 104), dp(4))); // Dark Navy background
                        langBanner.setPadding(dp(8), dp(6), dp(8), dp(6));

                        LinearLayout.LayoutParams lpBanner = new LinearLayout.LayoutParams(-1, -2);
                        lpBanner.setMargins(0, dp(8), 0, dp(4));
                        cartContainer.addView(langBanner, lpBanner);

                        sectionHeaderDrawn = true;
                    }

                    LinearLayout itemRow = new LinearLayout(this);
                    itemRow.setOrientation(LinearLayout.HORIZONTAL);
                    itemRow.setGravity(Gravity.CENTER_VERTICAL);
                    itemRow.setPadding(dp(6), dp(6), dp(6), dp(6));
                    itemRow.setBackground(bg(Color.rgb(245, 247, 252), dp(6)));

                    LinearLayout.LayoutParams lpRow = new LinearLayout.LayoutParams(-1, -2);
                    lpRow.setMargins(0, dp(3), 0, dp(3));
                    itemRow.setLayoutParams(lpRow);

                    // Book name in its language's unique text color (Non-red)
                    TextView nameTv = new TextView(this);
                    nameTv.setText(b.name);
                    nameTv.setTextSize(12.5f);
                    nameTv.setTypeface(null, Typeface.BOLD);
                    nameTv.setTextColor(langColors[i]); // Distinct language color
                    itemRow.addView(nameTv, new LinearLayout.LayoutParams(0, -2, 1f));

                    // Qty Controls (- qv +)
                    LinearLayout qtyBox = new LinearLayout(this);
                    qtyBox.setGravity(Gravity.CENTER);

                    TextView minus = new TextView(this);
                    minus.setText("−");
                    minus.setTextSize(15);
                    minus.setTypeface(null, Typeface.BOLD);
                    minus.setTextColor(Color.WHITE);
                    minus.setGravity(Gravity.CENTER);
                    minus.setBackground(bg(Color.rgb(220, 38, 38), dp(11)));
                    minus.setClickable(true);

                    TextView qv = tv("" + b.qty, Color.rgb(20, 35, 60));
                    qv.setTypeface(null, Typeface.BOLD);
                    qv.setGravity(Gravity.CENTER);

                    TextView plus = new TextView(this);
                    plus.setText("+");
                    plus.setTextSize(15);
                    plus.setTypeface(null, Typeface.BOLD);
                    plus.setTextColor(Color.WHITE);
                    plus.setGravity(Gravity.CENTER);
                    plus.setBackground(bg(Color.rgb(22, 163, 74), dp(11)));
                    plus.setClickable(true);

                    qtyBox.addView(minus, new LinearLayout.LayoutParams(dp(23), dp(23)));
                    qtyBox.addView(qv, new LinearLayout.LayoutParams(dp(22), dp(23)));
                    qtyBox.addView(plus, new LinearLayout.LayoutParams(dp(23), dp(23)));
                    itemRow.addView(qtyBox);

                    TextView priceTv = tv("₹" + bookTotal, purple);
                    priceTv.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
                    priceTv.setTypeface(null, Typeface.BOLD);
                    itemRow.addView(priceTv, new LinearLayout.LayoutParams(dp(62), -2));

                    TextView delBtn = new TextView(this);
                    delBtn.setText("🗑");
                    delBtn.setTextSize(13);
                    delBtn.setGravity(Gravity.CENTER);
                    delBtn.setPadding(dp(6), 0, dp(2), 0);
                    delBtn.setClickable(true);
                    itemRow.addView(delBtn);

                    minus.setOnClickListener(v -> {
                        if (f.qty > 0) f.qty--;
                        updateTotals();
                        render(search.getText().toString());
                        refreshCartView(cartContainer, cartTotalTxt);
                    });

                    plus.setOnClickListener(v -> {
                        f.qty++;
                        updateTotals();
                        render(search.getText().toString());
                        refreshCartView(cartContainer, cartTotalTxt);
                    });

                    delBtn.setOnClickListener(v -> {
                        f.qty = 0;
                        updateTotals();
                        render(search.getText().toString());
                        refreshCartView(cartContainer, cartTotalTxt);
                    });

                    cartContainer.addView(itemRow);
                }
            }
        }

        if (totalCartCount == 0) {
            TextView emptyTv = new TextView(this);
            emptyTv.setText("ఎంపిక చేసిన గ్రంథములు ఏవీ లేవు");
            emptyTv.setTextSize(13);
            emptyTv.setTextColor(Color.GRAY);
            emptyTv.setGravity(Gravity.CENTER);
            emptyTv.setPadding(0, dp(12), 0, dp(12));
            cartContainer.addView(emptyTv);
        }

        cartTotalTxt.setText("మొత్తం వెల: ₹" + String.format(Locale.US, "%,d", grandTotal) + ".00");
    }

    @SuppressWarnings("deprecation")
    void makePdf(String name, String committee, String phone, String address) {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("PDF తయారు చేయబడుతోంది...");
        pd.setCancelable(false);
        pd.show();

        new Thread(() -> {
            try {
                File dir = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "IGV");
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File file = new File(dir, "IGV_గ్రంథముల_జాబితా.pdf");

                PdfDocument doc = new PdfDocument();
                int W = 595, H = 842; // Standard A4 Size
                PdfDocument.PageInfo pi = new PdfDocument.PageInfo.Builder(W, H, 1).create();
                PdfDocument.Page p = doc.startPage(pi);
                Canvas c = p.getCanvas();

                Paint pTitle = new Paint(Paint.ANTI_ALIAS_FLAG);
                pTitle.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
                pTitle.setTextAlign(Paint.Align.CENTER);
                pTitle.setTextSize(17);
                pTitle.setColor(Color.rgb(18, 59, 104));

                Paint pText = new Paint(Paint.ANTI_ALIAS_FLAG);
                pText.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
                pText.setTextSize(10.5f);
                pText.setColor(Color.rgb(30, 30, 30));

                Paint pBold = new Paint(Paint.ANTI_ALIAS_FLAG);
                pBold.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
                pBold.setTextSize(10.5f);
                pBold.setColor(Color.rgb(18, 59, 104));

                Paint pLine = new Paint(Paint.ANTI_ALIAS_FLAG);
                pLine.setColor(Color.rgb(200, 200, 200));
                pLine.setStrokeWidth(1);

                // Document Title & Subtitle
                c.drawText("ఇందూ జ్ఞానవేదిక ప్రచురణలు", W / 2f, 26, pTitle);

                Paint pSubTitle = new Paint(Paint.ANTI_ALIAS_FLAG);
                pSubTitle.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
                pSubTitle.setTextSize(10f);
                pSubTitle.setTextAlign(Paint.Align.CENTER);
                pSubTitle.setColor(green);
                c.drawText("“త్రైత సిద్ధాంత ఆధ్యాత్మిక గ్రంథములు మానవ జీవితమునకు గొప్ప వెలుగు”", W / 2f, 41, pSubTitle);

                c.drawLine(20, 48, W - 20, 48, pLine);

                // Your Info Section (Compact 2 Lines)
                String dateStr = new SimpleDateFormat("dd/MM/yyyy, hh:mm a", Locale.US).format(new Date());

                c.drawText("వివరములు:", 25, 62, pBold);
                c.drawText("పేరు: " + name + "    కమిటీ: " + committee, 25, 76, pText);
                c.drawText("ఫోన్: " + phone + "    తేదీ: " + dateStr + "    చిరునామా: " + address, 25, 90, pText);

                c.drawLine(20, 98, W - 20, 98, pLine);

                int y = 108;
                int grandTotal = 0;
                int grandCount = 0;
                int serialNo = 1;

                // Count how many distinct languages have selected books
                int activeLanguagesCount = 0;
                for (String key : langKeys) {
                    ArrayList<Book> bList = langMap.get(key);
                    if (bList != null) {
                        boolean hasBooks = false;
                        for (Book b : bList) {
                            if (b.qty > 0) {
                                hasBooks = true;
                                break;
                            }
                        }
                        if (hasBooks) activeLanguagesCount++;
                    }
                }

                // Iterate language-wise to draw language sections, sub-totals, and grand total
                for (int i = 0; i < langKeys.length; i++) {
                    ArrayList<Book> bList = langMap.get(langKeys[i]);
                    if (bList == null) continue;

                    int langSubTotal = 0;
                    int langSubCount = 0;
                    boolean sectionHeaderDrawn = false;
                    int pdfLangColor = langColors[i];
                    String pdfLangHeader = pdfLangTitles[i];

                    for (Book b : bList) {
                        if (b.qty > 0) {
                            if (y > 810) {
                                doc.finishPage(p);
                                pi = new PdfDocument.PageInfo.Builder(W, H, doc.getPages().size() + 1).create();
                                p = doc.startPage(pi);
                                c = p.getCanvas();
                                y = 30;
                            }

                            // Draw Language Section Banner & Table Header
                            if (!sectionHeaderDrawn) {
                                // 1. Centered Language Heading Banner (e.g. తెలుగు గ్రంథములు)
                                Paint pLangBg = new Paint();
                                pLangBg.setColor(Color.rgb(18, 59, 104));
                                c.drawRect(20, y, W - 20, y + 18, pLangBg);

                                Paint pLangTxt = new Paint(Paint.ANTI_ALIAS_FLAG);
                                pLangTxt.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
                                pLangTxt.setTextSize(10.5f);
                                pLangTxt.setTextAlign(Paint.Align.CENTER);
                                pLangTxt.setColor(Color.rgb(255, 215, 0));

                                c.drawText(pdfLangHeader, W / 2f, y + 13, pLangTxt);
                                y += 22;

                                // 2. Table Header
                                Paint pHeaderBg = new Paint();
                                pHeaderBg.setColor(Color.rgb(94, 53, 177));
                                c.drawRect(20, y, W - 20, y + 18, pHeaderBg);

                                Paint pHeaderTxt = new Paint(Paint.ANTI_ALIAS_FLAG);
                                pHeaderTxt.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
                                pHeaderTxt.setTextSize(9.5f);
                                pHeaderTxt.setColor(Color.WHITE);

                                c.drawText("క్ర.సం", 25, y + 13, pHeaderTxt);
                                c.drawText("గ్రంథం పేరు", 70, y + 13, pHeaderTxt);
                                c.drawText("ధర", 380, y + 13, pHeaderTxt);
                                c.drawText("సంఖ్య", 440, y + 13, pHeaderTxt);
                                c.drawText("మొత్తం", 500, y + 13, pHeaderTxt);

                                y += 32; // Adequate space below table header so first book row never merges!
                                sectionHeaderDrawn = true;
                            }

                            int rowTotal = b.qty * b.price;
                            langSubTotal += rowTotal;
                            langSubCount += b.qty;
                            grandTotal += rowTotal;
                            grandCount += b.qty;

                            // Book Row Paint with unique non-red language color
                            Paint pBookRow = new Paint(Paint.ANTI_ALIAS_FLAG);
                            pBookRow.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
                            pBookRow.setTextSize(10f);
                            pBookRow.setColor(pdfLangColor);

                            c.drawText(String.valueOf(serialNo++), 25, y, pBookRow);

                            String bookName = b.name;
                            if (bookName.length() > 38) bookName = bookName.substring(0, 36) + "..";
                            c.drawText(bookName, 70, y, pBookRow);

                            c.drawText("₹" + b.price, 380, y, pBookRow);
                            c.drawText(String.valueOf(b.qty), 440, y, pBookRow);
                            c.drawText("₹" + rowTotal, 500, y, pBookRow);

                            y += 16;
                        }
                    }

                    // Draw Language Sub-Total ONLY IF books from 2 or more languages are selected!
                    if (sectionHeaderDrawn && activeLanguagesCount > 1) {
                        c.drawLine(20, y - 3, W - 20, y - 3, pLine);

                        Paint pSubTotal = new Paint(Paint.ANTI_ALIAS_FLAG);
                        pSubTotal.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
                        pSubTotal.setTextSize(10f);
                        pSubTotal.setColor(Color.rgb(21, 148, 71));

                        c.drawText(pdfLangHeader + " మొత్తం: " + langSubCount + " గ్రంథములు", 25, y + 12, pSubTotal);
                        c.drawText("₹" + String.format(Locale.US, "%,d", langSubTotal) + ".00", 500, y + 12, pSubTotal);

                        y += 24;
                    }
                }

                // Draw Totals at bottom
                c.drawLine(20, y - 3, W - 20, y - 3, pLine);
                y += 16;

                pBold.setTextSize(11.5f);
                pBold.setColor(Color.rgb(94, 53, 177));
                c.drawText("మొత్తం అన్ని గ్రంథములు: " + grandCount, 25, y, pBold);
                c.drawText("మొత్తం వెల: ₹" + String.format(Locale.US, "%,d", grandTotal) + ".00", 350, y, pBold);

                doc.finishPage(p);

                FileOutputStream out = new FileOutputStream(file);
                doc.writeTo(out);
                out.close();
                doc.close();

                // Share using FileProvider
                Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
                Intent in = new Intent(Intent.ACTION_SEND);
                in.setType("application/pdf");
                in.putExtra(Intent.EXTRA_STREAM, uri);
                in.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                runOnUiThread(() -> {
                    if (pd.isShowing()) pd.dismiss();
                    try {
                        startActivity(Intent.createChooser(in, "PDF పంచుకోండి"));
                    } catch (Exception ex) {
                        Toast.makeText(this, "PDF తయారైంది: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    if (pd.isShowing()) pd.dismiss();
                    Toast.makeText(this, "PDF తయారీలో సమస్య: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    int dp(int n) {
        return (int) (n * getResources().getDisplayMetrics().density + .5f);
    }
}
