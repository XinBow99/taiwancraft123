package com.xinbow99.taiwan.entity;

import com.xinbow99.taiwan.Taiwan;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;

/**
 * 8+9 的六種型。
 *
 * <h2>「8+9」不是罵人，是一個次文化的代稱</h2>
 * <p>這個詞源自「八家將」的台語諧音。八家將本來是廟宇陣頭——農閒時村裡的男丁在廟埕
 * 練陣、練武，既是信仰活動也是村里的凝聚方式。後來因為參與者多是社經地位較低的年輕
 * 男性，加上少數幫派借陣頭吸收少年，這個詞才被拿來當標籤。
 *
 * <p>這件事直接影響到這裡的設計：{@link #TEMPLE} 那一型講的是**信仰**，
 * {@link #WHITE} 那一型講的是**媽祖保佑**——他們不是流氓，是真的在扛轎。
 * 如果六型全部寫成尬車嗆聲，那就只是重複那個刻板印象，不是在做這個題材。
 *
 * <h2>台詞分單人與群聚兩組</h2>
 * <p>同一個人落單跟成群時講的話不一樣，這是這個族群最好認的特徵：人多的時候是
 * 招呼、是揪團、是「兄弟們」；一個人的時候音量會降下來。群聚的判定見
 * {@link EightNine}。
 */
public enum EightNineVariant implements StringRepresentable {

    /** 廟會扛轎哥。紅頭巾、龍紋圍兜、赤膊、白褲。 */
    TEMPLE("temple",
            new String[] {
                "兄弟們出轎啦，今天沒扛爆不回家！",
                "誰敢動我轎班的，喊一聲我整條街衝過去！",
                "鑼鼓敲起來，今天一定要熱到天上去！",
                "腳步踏穩，幹，陣容不能亂！",
            },
            new String[] {
                "轎槓上肩，這條路我走過幾十遍了。",
                "幹，這個重量扛久了肩膀是會記住的。",
                "今天天氣好，出轎順。",
            },
            new String[] {
                    "%s，你也是走陣的？跟我們走一趟啦！",
                    "%s 幹，你站這裡擋到轎了，讓一下。",
                    "%s 敢不敢跟我扛一段？肩膀會記住的。",
            }),

    /** 機車少年。黑帽 T、工作褲、側背包。 */
    RIDER("rider",
            new String[] {
                "這台剛拉轉，幹，聲浪直接炸整條！",
                "晚上12點集合，敢不敢跟我尬一波？",
                "引擎沒炸就是沒靈魂啦，懂？",
                "走啦，前面那個路口甩過去給你看！",
            },
            new String[] {
                "幹，這聲音不對，回去再調。",
                "油門一催就知道有沒有調好。",
                "等一下再去繞一圈。",
            },
            new String[] {
                    "%s，晚上十二點，敢不敢跟我尬一波？",
                    "%s 你那台幾匹的？拉來聽聽啊。",
                    "%s 幹，你這樣騎會被電啦，跟我走。",
            }),

    /** 潮流街頭仔。黑 T 翅膀圖騰、破牛仔褲、拖鞋、刺青。 */
    STREET("street",
            new String[] {
                "幹，這套今天直接殺爆全場，拍限動快！",
                "你那件？輸我三條街啦。",
                "氣勢不能輸，走路都要帶風！",
                "全場就我們這桌最亮，看什麼看。",
            },
            new String[] {
                "這件限量的，排三個小時。",
                "幹，鞋子髒了。",
                "今天光線不錯。",
            },
            new String[] {
                    "%s 幹，你這身在哪買的？",
                    "%s，比一下啊，看誰氣勢強。",
                    "%s 你敢不敢站過來跟我拍一張？",
            }),

    /** 地方大哥型。龍紋襯衫、金鍊、墨鏡。 */
    BOSS("boss",
            new String[] {
                "今天帳我包，幹，開心就對了！",
                "誰不爽？來跟我喝一杯！",
                "外面有事報我名，沒在怕的。",
                "坐啦坐啦，站著幹嘛。",
            },
            new String[] {
                "幹，人呢，都跑去哪了。",
                "這條街我看著長大的。",
                "等一下還有一攤。",
            },
            new String[] {
                    "%s 過來坐啦，這攤我的。",
                    "%s，外面有事報我名，沒在怕的。",
                    "%s 幹，你這個表不錯喔，哪買的。",
            }),

    /** 白衣白褲信徒。全白、佛珠。 */
    WHITE("white",
            new String[] {
                "媽祖保佑，幹，心誠就一定平安啦！",
                "一路順風順水，大家跟緊我！",
                "白衣白褲就是最帥，吵什麼吵。",
                "香點好，別亂跑。",
            },
            new String[] {
                "心誠則靈，這句是真的。",
                "幹，香灰掉到褲子上了。",
                "今天先去廟裡拜一下。",
            },
            new String[] {
                    "%s，一起去廟裡拜一下啦。",
                    "%s 幹，你身上沒帶香喔？",
                    "%s 跟緊我，今天保證你順。",
            }),

    /** 夜市兄弟團。深色連帽外套、工作褲、側背包。 */
    NIGHT_MARKET("night_market",
            new String[] {
                "走啦幹，宵夜吃起來，鹽酥雞點到滿！",
                "全部人給我到，少一個我記得住。",
                "今天沒晃到凌晨不算兄弟啦！",
                "那攤要排，但值得。",
            },
            new String[] {
                "幹，這攤收了喔。",
                "一個人吃也是要吃。",
                "老闆，一份鹽酥雞不要辣。",
            },
            new String[] {
                    "%s 走啦，宵夜吃起來！",
                    "%s，你敢不敢吃十份鹽酥雞？",
                    "%s 幹，這攤你沒吃過就不算來過。",
            });

    private static final EightNineVariant[] BY_ID = values();

    private final String name;
    private final Identifier texture;
    private final String[] crowdLines;
    private final String[] soloLines;
    /** 對著玩家講的。每一句都帶一個 %s，會換成玩家名字。 */
    private final String[] playerLines;

    EightNineVariant(String name, String[] crowdLines, String[] soloLines,
                     String[] playerLines) {
        this.name = name;
        this.texture = Taiwan.id("textures/entity/eightnine/" + name + ".png");
        this.crowdLines = crowdLines;
        this.soloLines = soloLines;
        this.playerLines = playerLines;
    }

    public Identifier texture() {
        return this.texture;
    }

    // ---- 配件 ---------------------------------------------------------------
    //
    // 六型共用一個模型，配件是靠零件的 visible 開關做的（見 EightNineModel）。
    // 為每一型各做一個模型的話，六份幾何要一起維護，改一個手臂長度要改六次。
    //
    // 判斷寫在 enum 上而不是算繪端：這是「這一型長什麼樣」的一部分，
    // 跟貼圖同一個層級的資料，不該散在 client 那邊。

    /** 墨鏡。大哥型的招牌。 */
    public boolean hasShades() {
        return this == BOSS;
    }

    /** 紅頭巾（含後面那條垂下來的）。陣頭的裝束。 */
    public boolean hasHeadband() {
        return this == TEMPLE;
    }

    /** 金鍊。大哥型跟街頭仔都戴，只是一個是真的一個是造型。 */
    public boolean hasChain() {
        return this == BOSS || this == STREET;
    }

    /** 側背包。機車少年與夜市團——那是真的在裝東西的包。 */
    public boolean hasBag() {
        return this == RIDER || this == NIGHT_MARKET;
    }

    /**
     * 挑一句台詞。
     *
     * @param crowd 旁邊有沒有同伴（見 {@link EightNine#CROWD}）
     */
    public String line(RandomSource random, boolean crowd) {
        String[] pool = crowd ? this.crowdLines : this.soloLines;
        return pool[random.nextInt(pool.length)];
    }

    /**
     * 對著某個玩家講的一句，名字會被帶進去。
     *
     * <p>這一組跟另外兩組的差別不只是多一個名字：**它是有對象的**。前兩組是自言自語
     * 或對同伴喊話，玩家只是剛好聽到；這一組是衝著你來的——約你尬車、問你衣服哪買的、
     * 叫你過來坐。同一個 NPC 講前兩組跟講這一組，玩家的感受完全不一樣。
     */
    public String lineFor(RandomSource random, String playerName) {
        return this.playerLines[random.nextInt(this.playerLines.length)].formatted(playerName);
    }

    /**
     * 從同步過來的序號還原。
     *
     * <p>超出範圍就退回第一個而不是丟例外：這個值會從網路與存檔進來，舊存檔沒有這個
     * 欄位、或是以後刪掉某一型時都會讀到對不上的數字，那時候該顯示成某一型，
     * 不是讓整個世界載入失敗。
     */
    public static EightNineVariant byId(int id) {
        return id >= 0 && id < BY_ID.length ? BY_ID[id] : TEMPLE;
    }

    public static EightNineVariant byName(String name) {
        for (EightNineVariant variant : BY_ID) {
            if (variant.name.equals(name)) return variant;
        }
        return TEMPLE;
    }

    public static EightNineVariant random(RandomSource random) {
        return BY_ID[random.nextInt(BY_ID.length)];
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
