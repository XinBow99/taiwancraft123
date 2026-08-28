package com.xinbow99.taiwan.worldgen;

/**
 * 招牌上的字。**全部原創惡搞，不使用任何真實商標。**
 *
 * <h2>為什麼招牌一定要有字</h2>
 * <p>一塊彩色的板子只是一塊彩色的板子。台灣街景之所以是台灣街景，是因為那些板子上
 * 擠滿了字——而且是**直排、擠、字比板子大**的那種。有字跟沒字，是「某個亞洲城市」跟
 * 「台灣」的差別。
 *
 * <h2>惡搞的分寸</h2>
 * <p>取名的方式是**同音替字**（清心→清新、五十→陸拾），保留原本的語感節奏但換掉字。
 * 讀者一眼認得出在講哪一類店，卻不是任何一家真的店。全部另創一組毫無關聯的名字反而失敗：
 * 那樣的招牌讀起來像奇幻遊戲，不像台灣。
 *
 * <p>這裡不用任何真實企業的名稱、商標或標語。
 */
public final class ShopName {

    /** 手搖飲料店。 */
    private static final String[][] DRINK = {
            {"陸拾嵐", "手搖茶飲"},
            {"清新福全", "青茶專賣"},
            {"迷客冬", "鮮奶茶"},
            {"珍煮母", "黑糖珍珠"},
            {"可不可可", "紅茶專門"},
            {"龜記茶枋", "手作茶飲"},
            {"大宛仔", "檸檬綠茶"},
            {"鶴茶館", "職人茶飲"},
            {"麻吉茶飲", "波霸奶茶"},
            {"阿嬤ㄟ茶", "古早味紅茶"},
            {"茶湯薈", "四季春"},
            {"日出禾茶", "冷萃茶"},
    };

    /** 便利商店。 */
    private static final String[][] CONVENIENCE = {
            {"柒拾壹", "24H 便利商店"},
            {"全佳超商", "24 小時營業"},
            {"萊爾福", "便利生活"},
            {"OK啦超商", "隨時為你開"},
            {"順超商", "關東煮熱賣"},
            {"好鄰居", "24H"},
    };

    /**
     * 透天厝一樓的招牌。台灣的街屋一樓不是店就是車庫，而車庫的鐵門上通常有這幾種字。
     */
    private static final String[][] TENEMENT = {
            {"吉屋出租", "電洽 0900-", "000-000"},
            {"車庫", "門口請勿停車"},
            {"頂樓雅房", "出租"},
            {"私人車位", "拖吊送辦"},
            {"阿源鐵工廠", "鐵門・鐵窗"},
            {"美滿命相館", "看日・改名"},
    };

    /** 夜市攤位。 */
    private static final String[][] STALL = {
            {"蚵仔煎", "一份 60"},
            {"大腸包小腸", "一支 50"},
            {"鹹酥雞", "秤重計價"},
            {"臭豆腐", "外酥內軟"},
            {"藥燉排骨", "冬令進補"},
            {"車輪餅", "紅豆・奶油"},
            {"撈金魚", "一網 50"},
            {"射氣球", "三發 100"},
            {"棺材板", "府城名產"},
            {"青蛙下蛋", "古早味"},
    };

    /** 宮廟。廟名沒有商標問題，但一樣全部自創，避開任何實際存在的廟。 */
    private static final String[][] TEMPLE = {
            {"鎮安宮", "風調雨順"},
            {"慈雲宮", "國泰民安"},
            {"金順宮", "合境平安"},
            {"福德祠", "土地公廟"},
            {"保生殿", "有求必應"},
            {"天海宮", "四海昇平"},
    };

    private ShopName() {
    }

    public static String[] drink(int salt) {
        return pick(DRINK, salt);
    }

    public static String[] convenience(int salt) {
        return pick(CONVENIENCE, salt);
    }

    public static String[] tenement(int salt) {
        return pick(TENEMENT, salt);
    }

    public static String[] stall(int salt) {
        return pick(STALL, salt);
    }

    public static String[] temple(int salt) {
        return pick(TEMPLE, salt);
    }

    private static String[] pick(String[][] table, int salt) {
        return table[Math.floorMod(salt, table.length)];
    }
}
