package com.mandarincoach.app.data.repository

import com.mandarincoach.app.data.model.ProficiencyLevel

object VocabularyRepository {

    private val beginnerWords = listOf(
        // Greetings & basic communication
        "你好", "再见", "谢谢", "对不起", "没关系", "请", "是", "不是", "好", "不好",
        "你", "我", "他", "她", "我们", "你们", "他们",
        // Numbers
        "一", "二", "三", "四", "五", "六", "七", "八", "九", "十",
        "百", "千", "零", "多少", "几",
        // Family
        "爸爸", "妈妈", "哥哥", "姐姐", "弟弟", "妹妹", "儿子", "女儿", "朋友",
        // Time
        "今天", "明天", "昨天", "现在", "早上", "中午", "晚上", "年", "月", "日",
        "星期", "小时", "分钟",
        // Basic verbs
        "是", "有", "吃", "喝", "去", "来", "看", "说", "听", "想", "要", "喜欢",
        "工作", "学习", "睡觉", "起床", "买", "用",
        // Food & drink
        "水", "茶", "咖啡", "米饭", "面条", "包子", "饺子", "菜", "肉", "鱼",
        "苹果", "香蕉", "水果",
        // Places
        "中国", "学校", "家", "餐厅", "医院", "商店", "银行", "机场",
        // Adjectives
        "大", "小", "多", "少", "好", "坏", "新", "旧", "漂亮", "高兴", "忙",
        // Questions
        "什么", "哪里", "谁", "怎么", "为什么", "哪个", "多少",
        // Colors
        "红", "蓝", "绿", "白", "黑", "黄"
    )

    private val intermediateWords = listOf(
        // Emotions & states
        "高兴", "难过", "生气", "害怕", "惊讶", "担心", "放松", "紧张", "无聊", "有趣",
        // Travel
        "旅游", "飞机", "火车", "地铁", "公共汽车", "出租车", "护照", "签证", "宾馆",
        "景点", "地图", "方向", "左边", "右边", "前面", "后面",
        // Shopping
        "价格", "便宜", "贵", "折扣", "信用卡", "现金", "超市", "市场", "换",
        // Health
        "身体", "头疼", "感冒", "发烧", "药", "医生", "看病", "运动", "健康",
        // Work & study
        "公司", "老板", "同事", "会议", "报告", "项目", "电脑", "手机",
        "大学", "专业", "考试", "成绩", "老师", "学生",
        // Weather
        "天气", "晴天", "下雨", "下雪", "风", "温度", "冷", "热", "凉快",
        // Hobbies
        "音乐", "电影", "书", "运动", "旅行", "做饭", "画画", "唱歌",
        // Grammar helpers
        "因为", "所以", "但是", "虽然", "如果", "已经", "还没", "刚才", "以后",
        "一起", "经常", "有时候", "从来", "可以", "应该", "必须", "能",
        // Extended verbs
        "帮助", "告诉", "觉得", "知道", "了解", "记得", "忘记", "希望", "决定",
        "参加", "准备", "开始", "结束", "介绍", "比较", "改变"
    )

    private val advancedWords = listOf(
        // Abstract concepts
        "文化", "传统", "历史", "社会", "经济", "政治", "环境", "科技", "教育",
        "发展", "影响", "关系", "问题", "解决", "机会", "挑战", "责任",
        // Formal language
        "根据", "关于", "对于", "由于", "此外", "另外", "总的来说", "事实上",
        "从而", "因此", "尽管", "然而", "相比之下", "值得注意",
        // Business & professional
        "合同", "谈判", "合作", "投资", "市场", "竞争", "策略", "目标",
        "效率", "质量", "创新", "品牌",
        // Idioms (成语)
        "马到成功", "一石二鸟", "半途而废", "一步一个脚印", "功夫不负有心人",
        "三人行必有我师", "活到老学到老", "万事开头难", "百闻不如一见",
        // Complex verbs & phrases
        "意味着", "体现", "反映", "强调", "提高", "降低", "实现", "避免",
        "导致", "促进", "阻碍", "依赖", "证明", "分析", "评价",
        // Culture & arts
        "文学", "艺术", "哲学", "诗歌", "小说", "电视剧", "节日", "习俗",
        "春节", "中秋节", "端午节", "饮食文化",
        // News vocabulary
        "报道", "事件", "政策", "法律", "权利", "义务", "民主", "自由"
    )

    private val fluentWords = listOf(
        // Sophisticated idioms
        "锦上添花", "雪中送炭", "画龙点睛", "一鸣惊人", "脱颖而出",
        "举一反三", "融会贯通", "博古通今", "厚积薄发", "游刃有余",
        // Colloquial expressions
        "加油", "没问题", "差不多", "随便", "算了", "哎呀", "原来如此",
        "不得了", "了不起", "太棒了", "厉害", "牛", "靠谱", "不靠谱",
        // Nuanced vocabulary
        "微妙", "复杂", "矛盾", "讽刺", "隐喻", "含义", "内涵", "外延",
        "本质", "现象", "规律", "逻辑", "辩证",
        // Literary & classical
        "诗情画意", "意境", "意象", "韵味", "气质", "风格", "境界",
        // Regional & contemporary slang
        "网红", "打卡", "种草", "躺平", "内卷", "996", "佛系",
        "破防", "上头", "绝绝子", "yyds", "暴击",
        // Philosophy & deep topics
        "人生观", "价值观", "世界观", "人生意义", "存在主义",
        "道德", "伦理", "公正", "平等", "自由意志"
    )

    fun getVocabularyForLevel(level: ProficiencyLevel): List<String> = when (level) {
        ProficiencyLevel.BEGINNER -> beginnerWords
        ProficiencyLevel.INTERMEDIATE -> beginnerWords + intermediateWords
        ProficiencyLevel.ADVANCED -> intermediateWords + advancedWords
        ProficiencyLevel.FLUENT -> advancedWords + fluentWords
    }

    /**
     * Extracts Chinese words from a string. Currently uses character-based 
     * segmentation but can be expanded to use a dictionary-based segmenter.
     */
    fun segment(text: String): Set<String> {
        return text.filter { it.code in 0x4E00..0x9FFF }
            .map { it.toString() }
            .filter { it.isNotBlank() }
            .toSet()
    }

    /**
     * Filters the global corpus based on HSK/Level ranking and user progress.
     * Ensures the bot primarily uses "learned" words and introduces a controlled amount of new ones.
     */
    fun getActiveVocabulary(
        level: ProficiencyLevel,
        learnedWords: Set<String>,
        newWordsTarget: Int
    ): List<String> {
        val fullCorpus = getVocabularyForLevel(level)
        
        // Words the user already knows
        val alreadyLearned = fullCorpus.filter { it in learnedWords }
        
        // New words to introduce (ranked by appearance in the corpus list)
        val newWords = fullCorpus.filter { it !in learnedWords }.take(newWordsTarget)
        
        return alreadyLearned + newWords
    }

    fun buildSystemPrompt(
        level: ProficiencyLevel,
        userName: String = "",
        learningGoals: String = "",
        interests: String = "",
        activeVocab: List<String> = emptyList(),
        conversationSummary: String = "",
        learningProfile: String = ""
    ): String = PromptBuilder.build(
        level = level,
        userName = userName,
        learningGoals = learningGoals,
        interests = interests,
        activeVocab = activeVocab,
        conversationSummary = conversationSummary,
        learningProfile = learningProfile
    )
}
