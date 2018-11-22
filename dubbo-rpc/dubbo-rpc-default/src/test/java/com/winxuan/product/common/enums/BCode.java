package com.winxuan.product.common.enums;

import com.google.common.collect.Sets;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

/**
 * 商品元数据
 *
 * @author Li Pengcheng
 * @version 1.0
 */
public enum BCode implements ICode {

    B_EXTEND_FORMAT(1L, "b_extend_format", "开本", String.class),
    B_EXTEND_PAGES(2L, "b_extend_pages", "页数", String.class),
    B_EXTEND_WORDS(3L, "b_extend_words", "字数", String.class),
    B_EXTEND_BINDING(4L, "b_extend_binding", "装帧", String.class),
    B_EXTEND_EDITION(5L, "b_extend_edition", "版次", String.class),
    B_EXTEND_IMPRESSION(6L, "b_extend_impression", "印次", String.class),
    B_EXTEND_PUBLICATION_DATE(7L, "b_extend_publication_date", "出版时间", String.class),
    B_EXTEND_PRINTING_TIME(8L, "b_extend_printing_time", "印刷时间", String.class),
    B_DESCRIPTION_EDITOR_RECOMMEND(9L, "b_description_editor_recommend", "主编推荐", String.class),
    B_DESCRIPTION_CONTENT_ABSTRACT(10L, "b_description_content_abstract", "内容简介", String.class),
    B_DESCRIPTION_DIGEST(11L, "b_description_digest", "摘要", String.class),
    B_DESCRIPTION_DIRECTORY(12L, "b_description_directory", "目录", String.class),
    B_DESCRIPTION_ABOUT_AUTHOR(13L, "b_description_about_author", "作者简介", String.class),
    B_EXTEND_DISCS_NUMBER(14L, "b_extend_discs_number", "碟片数量", String.class),
    B_EXTEND_RUNNING_TIME(15L, "b_extend_running_time", "片长", String.class),
    B_EXTEND_LANGUAGES(16L, "b_extend_languages", "语种", String.class),
    B_EXTEND_STAR(17L, "b_extend_star", "主演", String.class),
    B_DESCRIPTION_MEDIA_COMMENTARY(18L, "b_description_media_commentary", "媒体评论", String.class),
    B_DESCRIPTION_INTRODUCTION(23L, "b_description_introduction", "商品介绍", String.class),
    B_DESCRIPTION_SPECIFICATION(24L, "b_description_specification", "规格参数", String.class),
    B_DESCRIPTION_PACKING_LIST(25L, "b_description_packing_list", "包装清单", String.class),
    B_DESCRIPTION_AFTERSALES(26L, "b_description_aftersales", "售后服务", String.class),
    B_EXTEND_DIELECTRIC(33L, "b_extend_dielectric", "介质", String.class),
    B_DESCRIPTION_MEDIA_OLD(42L, "b_description_media_old", "媒体评论", String.class),
    B_DESCRIPTION_HIGHLIGHT(43L, "b_description_highlight", "精彩内容", String.class),
    B_PRODUCT_ISBN(44L, "b_product_isbn", "ISBN", String.class),
    B_EXTEND_BRANDS(45L, "b_extend_brands", "品牌", String.class),
    B_EXTEND_MATERIAL(46L, "b_extend_material", "材质", String.class),
    B_EXTEND_PRICE_SCOPE(47L, "b_extend_price_scope", "价格范围", String.class),
    B_EXTEND_TRANSLATOR(49L, "b_extend_translator", "译者", String.class),
    B_EXTEND_PRICE(50L, "b_extend_price", "价格", String.class),
    B_EXTEND_SIZE(51L, "b_extend_size", "尺寸", String.class),
    B_EXTEND_TIME(53L, "b_extend_time", "时间", String.class),
    B_EXTEND_BULB(54L, "b_extend_bulb", "灯泡", String.class),
    B_EXTEND_ILLUMINANT(55L, "b_extend_illuminant", "光源", String.class),
    B_EXTEND_BRIGHTNESS(56L, "b_extend_brightness", "亮度", String.class),
    B_EXTEND_DIMMER(57L, "b_extend_dimmer", "调光", String.class),
    B_EXTEND_FOCUS(58L, "b_extend_focus", "调焦", String.class),
    B_EXTEND_RANGE(59L, "b_extend_range", "射程", String.class),
    B_EXTEND_CHARGING(60L, "b_extend_charging", "充电", String.class),
    B_EXTEND_BATTERY(61L, "b_extend_battery", "电池", String.class),
    B_EXTEND_WATERPROOF(62L, "b_extend_waterproof", "防水", String.class),
    B_EXTEND_EFFICACY(63L, "b_extend_efficacy", "功效", String.class),
    B_EXTEND_CONSTITUENT(64L, "b_extend_constituent", "成分", String.class),
    B_EXTEND_SPECIFICATION(65L, "b_extend_specification", "规格", String.class),
    B_EXTEND_CAPACITY(66L, "b_extend_capacity", "容量", String.class),
    B_EXTEND_CROWD(67L, "b_extend_crowd", "人群", String.class),
    B_EXTEND_NAME(68L, "b_extend_name", "名称", String.class),
    B_EXTEND_MODEL(69L, "b_extend_model", "型号", String.class),
    B_EXTEND_FACTORY(70L, "b_extend_factory", "厂家", String.class),
    B_EXTEND_PRODUCING_AREA(71L, "b_extend_producing_area", "产地", String.class),
    B_EXTEND_GROSS_WEIGHT(72L, "b_extend_gross_weight", "毛重", String.class),
    B_EXTEND_WEIGHT(73L, "b_extend_weight", "重量", String.class),
    B_EXTEND_SERIES(74L, "b_extend_series", "丛书名", String.class),
    B_EXTEND_CLASSIFICATION(75L, "b_extend_classification", "分卷册次", String.class),
    B_EXTEND_CLASSIFICATION_NAME(76L, "b_extend_classification_name", "分卷册次题名", String.class),
    B_EXTEND_BOTTLE_CODE(77L, "b_extend_bottle_code", "版别代码", String.class),
    B_EXTEND_COOPERATION_PUBLISHER(78L, "b_extend_Cooperation_publisher", "合作出版者", String.class),
    @Deprecated
    B_EXTEND_CHIEF_EDITOR(79L, "b_extend_chief_editor", "主编", String.class),
    B_EXTEND_EDITOR(80L, "b_extend_editor", "编者", String.class),
    @Deprecated
    B_EXTEND_FIRST_EDITION(81L, "b_extend_first_edition", "首版年月", String.class),
    @Deprecated
    B_EXTEND_PRICING_CURRENCY(82L, "b_extend_pricing_currency", "定价货币", String.class),
    /**
     * 0-其他
     * 1-图书
     * 2-音带
     * 3-像带
     * 4-软盘
     * 5-CD-ROM
     * 6-CD
     * 7-VCD
     * 8-DVD
     * 9-点卡
     * 10-幻灯片
     * 11-投影片
     * 12-教学包
     * 13-地图/挂图
     * 14-儿童玩具
     * 15-标准
     * 16-DVD-5
     * 17-DVD-9
     * 18-BD-DVD
     */
    B_EXTEND_CARRIER(83L, "b_extend_carrier", "载体", String.class),
    B_EXTEND_SUBJECT_TERM(84L, "b_extend_subject_term", "主题词", String.class),
    B_EXTEND_CLC(85L, "b_extend_CLC", "中图法", String.class),
    @Deprecated
    B_EXTEND_SUBJECTS(86L, "b_extend_subjects", "科目", String.class),
    @Deprecated
    B_EXTEND_CLASS(87L, "b_extend_class", "年级", String.class),
    @Deprecated
    B_EXTEND_DISCIPLINE(88L, "b_extend_discipline", "学科", String.class),
    @Deprecated
    B_EXTEND_PUBLISHING_LOCATION(89L, "b_extend_publishing_location", "出版地", String.class),
    @Deprecated
    B_EXTEND_AWARDS(90L, "b_extend_awards", "获奖情况", String.class),
    @Deprecated
    B_EXTEND_IS_CONPLEX(91L, "b_extend_is_conplex", "是否套装书", String.class),
    B_EXTEND_ATTACHMENT(92L, "b_extend_attachment", "附件", String.class),
    B_EXTEND_READER(93L, "b_extend_reader", "读者对象", String.class),
    B_EXTEND_LENGTH(94L, "b_extend_length", "长", String.class),
    B_EXTEND_WIDTH(95L, "b_extend_width", "宽", String.class),
    B_EXTEND_THICK(96L, "b_extend_thick", "高/厚", String.class),
    @Deprecated
    B_EXTEND_AREA_CODE(98L, "b_extend_area_code", "区码", String.class),
    B_EXTEND_DIRECTOR(99L, "b_extend_director", "导演", String.class),
    B_EXTEND_LYRICS(100L, "b_extend_lyrics", "作词", String.class),
    B_EXTEND_COMPOSE(101L, "b_extend_compose", "作曲", String.class),
    B_EXTEND_BAND(102L, "b_extend_band", "乐队", String.class),
    B_EXTEND_MAIN_ACTOR(103L, "b_extend_main_actor", "主要演员", String.class),
    B_EXTEND_GENERAL_DISTRIBUTION(104L, "b_extend_general_distribution", "总发行", String.class),
    @Deprecated
    B_EXTEND_SUBTITLE(105L, "b_extend_subtitle", "副标题", String.class),
    @Deprecated
    B_EXTEND_CANON_NAME(106L, "b_extend_canon_name", "分册名", String.class),
    @Deprecated
    B_EXTEND_CANON_NUMBER(107L, "b_extend_canon_number", "分册号", String.class),
    B_EXTEND_ENGLISH_TITLE(108L, "b_extend_english_title", "英文书名", String.class),
    B_EXTEND_VERSION_NAME(109L, "b_extend_version_name", "版本名", String.class),
    B_EXTEND_BAND_NAME(110L, "b_extend_band_name", "品牌名", String.class),
    @Deprecated
    B_EXTEND_ANNUAL_NUMBER(111L, "b_extend_annual_number", "年号", String.class),
    @Deprecated
    B_EXTEND_TIED_SERIES(113L, "b_extend_tied_series", "并列丛书名", String.class),
    B_EXTEND_HAS_EPUB(114L, "b_extend_has_epub", "是否有epub", String.class),
    B_EXTEND_FREE(115L, "b_extend_free", "是否免费", String.class),
    B_EXTEND_READ_RANGE(116L, "b_extend_read_range", "读取范围", String.class),
    B_EXTEND_EBOOKS_PAGINATION(117L, "b_extend_ebooks_pagination", "电子书页数", String.class),
    B_EXTEND_CIP_DATA(118L, "b_extend_cip_data", "cip数据", String.class),
    B_EXTEND_COPYRIGHT_NAME(119L, "b_extend_copyright_name", "版权页书名", String.class),
    @Deprecated
    B_EXTEND_NATIONALITY(140L, "b_extend_nationality", "国籍", String.class),
    @Deprecated
    B_EXTEND_DYNASTY(141L, "b_extend_dynasty", "朝代", String.class),
    B_EXTEND_OTHER_AUTHER(142L, "b_extend_other_auther", "其他作者", String.class),
    B_EXTEND_CUSTOM_COMPLEX(143L, "b_extend_custom_complex", "是否定制套装书", String.class),
    @Deprecated
    B_EXTEND_COVER_NAME(144L, "b_extend_cover_name", "封面书名", String.class),
    B_EXTEND_AUTHOR(145L, "b_extend_author", "作者", String.class),
    B_DESCRIPTION_CELEBRITY_RECOMMEN(146L, "b_description_celebrity_recommen", "名人推荐", String.class),
    B_DESCRIPTION_SELLING_PROINT(147L, "b_description_selling_proint", "促销语", String.class),

    B_PRODUCT_ID(213L, "b_product_id", "商品编码", Long.class),
    B_PRODUCT_NAME(214L, "b_product_name", "商品名称", String.class),
    B_PRODUCT_MERCH_ID(215L, "b_product_merch_id", "MerchId", Long.class),
    B_PRODUCT_MANUFACTURER(216L, "b_product_manufacturer", "出版社", String.class),
    B_PRODUCT_PRODUCTION_DATE(217L, "b_product_production_date", "出版时间", Date.class),
    B_PRODUCT_SORT(218L, "b_product_sort", "商品种类", Long.class),
    B_PRODUCT_PRINT_DATE(219L, "b_product_print_date", "打印时间", Date.class),
    B_PRODUCT_AUTHOR(220L, "b_product_author", "作者", String.class),
    B_PRODUCT_LIST_PRICE(221L, "b_product_list_price", "码洋", BigDecimal.class),
    B_PRODUCT_MC(222L, "b_product_mc", "MC分类", String.class),
    B_PRODUCT_LOCKED(224L, "b_product_locked", "是否锁定", Boolean.class),
    B_PRODUCT_COMPLEX(225L, "b_product_complex", "套装信息", Integer.class),
    B_PRODUCT_HAS_IMG(226L, "b_product_has_img", "是否有图片", Boolean.class),
    B_PRODUCT_CREATE_TIME(227L, "b_product_create_time", "产品创建时间", Date.class),
    B_PRODUCT_UPDATE_TIME(228L, "b_product_update_time", "产品更新时间", Date.class),

    B_PRODUCT_SALE_ID(229L, "b_product_sale_id", "EC编码", Long.class),
    B_PRODUCT_SALE_SHOP(230L, "b_product_sale_shop", "卖家店铺", Long.class),
    B_PRODUCT_SALE_NAME(231L, "b_product_sale_name", "销售名称", String.class),
    B_PRODUCT_SALE_STATUS(232L, "b_product_sale_status", "销售状态", Long.class),
    B_PRODUCT_SALE_AUDITS_STATUS(233L, "b_product_sale_audits_status", "审核状态", Long.class),
    B_PRODUCT_SALE_SUPPLY_TYPE(234L, "b_product_sale_supply_type", "供应类型", Long.class),
    B_PRODUCT_SALE_STORAGE_TYPE(235L, "b_product_sale_storage_type", "存储类型", Long.class),
    B_PRODUCT_SALE_OUTER_ID(236L, "b_product_sale_outer_id", "SAP编码", String.class),
    B_PRODUCT_SALE_BUNDLE(237L, "b_product_sale_bundle", "是否搭配商品", Integer.class),
    B_PRODUCT_SALE_VENDOR(238L, "b_product_sale_vendor", "供应商", String.class),
    B_PRODUCT_SALE_WORK_CATEGORY(239L, "b_product_sale_work_category", "经营分类", String.class),
    B_PRODUCT_SALE_MANAGE_CATEGORY(240L, "b_product_sale_manage_category", "管理分类", String.class),
    B_PRODUCT_SALE_SELLING_POINTS(241L, "b_product_sale_selling_points", "卖点", String.class),
    B_PRODUCT_SALE_CREATE_TIME(242L, "b_product_sale_create_time", "商品创建时间", Date.class),
    B_PRODUCT_SALE_UPDATE_TIME(243L, "b_product_sale_update_time", "商品更新时间", Date.class),

    B_BOOKING_STOCK(244L, "b_booking_stock", "预售库存", Integer.class),
    B_BOOKING_START_TIME(245L, "b_booking_start_time", "预售开始时间", Date.class),
    B_BOOKING_END_TIME(246L, "b_booking_end_time", "预售结束时间", Date.class),
    B_BOOKING_DC(247L, "b_booking_dc", "预售仓库", Long.class),


    B_PRODUCT_SALE_GRADE(264L, "b_product_sale_grade", "当月销售等级", String.class),
    B_PRODUCT_SALE_GRADE1(265L, "b_product_sale_grade1", "1个月前销售等级", String.class),
    B_PRODUCT_SALE_GRADE2(266L, "b_product_sale_grade2", "2个月前销售等级", String.class),
    B_PRODUCT_SALE_GRADE3(267L, "b_product_sale_grade3", "3个月前销售等级", String.class),
    B_PRODUCT_SALE_GRADE4(268L, "b_product_sale_grade4", "4个月前销售等级", String.class),
    B_PRODUCT_SALE_GRADE5(269L, "b_product_sale_grade5", "5个月前销售等级", String.class),

    B_STOCK(270L, "b_stock", "渠道库存", Integer.class),//无法查，待确定
    B_RATE_PURCHASE(271L, "b_rate_purchase", "进价", BigDecimal.class),
    B_RATE_SALE_PRICE(272L, "b_rate_sale_price", "售价", BigDecimal.class),//无法查，待确定
    B_MODULE_MEETING_PLACE(273L, "b_module_meeting_place", "会场模板", String.class),//无法查，待确定
    B_MODULE_NAV_BAR(274L, "b_module_nav_bar", "导航模板", String.class),//无法查，待确定
    B_MODULE_CORRELATE(275L, "b_module_correlate", "关联推荐模板", String.class),//无法查，待确定
    B_PRODUCT_MC_NAME(276L, "b_product_mc_name", "MC分类名称", String.class),
    B_OPERATE_CATEGORY(277L, "b_operate_category", "营销分类", String.class),//取MC分类
    B_MAIN_CATEGORY_NAME(308L, "b_main_category_name", "九大类名称", String.class), // mc 对类九大类名称 无数据库对应

    /**
     * 无对应值, 不需要查数据
     **/
    B_PLATFORM_CATEGORY_NAME(309L, "b_platform_category_name", "平台分类名称", String.class), // 无数据库对应
    B_IMG_SHOP_PROMOTION(310L, "b_img_shop_promotion", "店铺促销图", Object.class), // 无数据库对应，仅仅用于通知
    B_IMG_SHOP_SPECIALTY(311L, "b_img_shop_specialty", "店铺个性图", Object.class), // 无数据库对应，仅仅用于通知
    B_EXTEND_RECOMMEND_TYPE(312L, "b_extend_recommend_type", "推荐类型", String.class),

    B_IS_ISBN_MULTI_BOOK(282L, "b_is_isbn_multi_book", "一号多书", Boolean.class),//计算出来的

    /**
     * 主数据同步重构新增字段
     */
    B_EXTEND_PUBLISHER_IDENTIFIER(283L, "b_extend_publisher_identifire", "出版社编码", String.class),
    B_VENDOR_NAME(284L, "b_vendor_name", "供应商名称", String.class),//来自vendor表
    B_EXTEND_FOREIGN_NAME(287L, "b_extend_foreign_name", "外文书名", String.class),//书名的外文译名
    B_EXTEND_TOME_COUNT(288L, "b_extend_tome_count", "册数", String.class),//套装书中含有单册的数量(数值)
    /**
     * 1	0岁—2岁
     * 2	3岁—6岁
     * 3	7岁—10岁
     * 4	11岁—14岁
     */
    B_EXTEND_AGE_GROUP(289L, "b_extend_age_group", "年龄段", String.class),//阅读对象的推荐年龄阶段
    /**
     * 1	小学一年级
     * 2	小学二年级
     * 3	小学三年级
     * 4	小学四年级
     * 5	小学五年级
     * 6	小学六年级
     * 7	七年级/初中一年级
     * 8	八年级/初中二年级
     * 9	九年级/初中三年级
     * 10	高中一年级
     * 11	高中二年级
     * 12	高中三年级
     * 13	大学一年级
     * 14	大学二年级
     * 15	大学三年级
     * 16	大学四年级
     * 17	研究生一年级
     * 18	研究生二年级
     * 19	研究生三年级
     */
    B_EXTEND_GRADE_GROUP(290L, "b_extend_grade_group", "学龄段", String.class),//阅读对象的推荐学龄阶段
    B_EXTEND_PRINTED_SHEET_AMOUNT(302L, "b_extend_printed_sheet_amount", "印张", String.class),//印刷该图书所用全开纸张数量(小数)
    B_EXTEND_IS_SOLD_OUT(305L, "b_extend_is_sold_out", "不允许上架标识", String.class),//1-不允许上架，0-允许上架
    /**
     * 00-无限制
     * 01-不进行替换而取消连接
     * 02-进行替换而取消连接
     * 03-技术问题
     * 04-测试部分
     * 11-正在开发
     * X2-停止销售
     * X3-停止收退（客户退货）
     * X4-修改评估类
     * X5-停止新华在线申请
     */
    B_EXTEND_SAP_DISTRIBUTION_STATUS(306L, "b_extend_sap_distribution_status", "SAP分销状态", String.class),
    B_IMG_SHOP_BIG_PULL(313L, "b_img_shop_big_pull", "店铺大拉页", Object.class),   // 无数据库对应，仅仅用于通知
    B_EXTEND_PAINTER(314L, "b_extend_painter", "绘者", String.class),

    B_DESCRIPTION_AWARDS(328L, "b_description_awards", "获奖情况", String.class),

    /**
     * 出版标识
     * 0-单品
     * 1-套装书
     * 2-组套书
     */
    B_EXTEND_PUBLICATION_TYPE(329L, "b_extend_publication_type", "出版标识", String.class),
    /**
     * 国际商品编码信息对的类别代码
     * Z1-EAN (内部分配可能)
     * Z2-ISBN（外部）
     * Z3-ISRC（外部）
     * Z4-ISSN（外部）
     * Z5-其他（外部）
     * Z8-EC套装（内部）
     * Z9-EC套装（外部）
     */
    B_EXTEND_NUMTP(330L, "b_extend_numtp", "国际商品编码信息", String.class);

    /**
     * 扩展信息BCode分组
     */
    public static final Set<BCode> EXTEND_BCODES;
    /**
     * 大字段信息BCode分组
     */
    public static final Set<BCode> DESCRIPTION_BCODES;
    /**
     * 产品信息（表）BCode分组
     */
    public static final Set<BCode> PRODUCT_BCODES;
    /**
     * 商品信息（表）BCode分组
     */
    public static final Set<BCode> PRODUCT_SALE_BCODES;


    static {
        EXTEND_BCODES = Collections.unmodifiableSet(Sets.newHashSet(BCode.B_EXTEND_FORMAT, BCode.B_EXTEND_PAGES, BCode.B_EXTEND_WORDS,
                BCode.B_EXTEND_BINDING, BCode.B_EXTEND_EDITION, BCode.B_EXTEND_IMPRESSION,
                BCode.B_EXTEND_PUBLICATION_DATE, BCode.B_EXTEND_PRINTING_TIME, BCode.B_EXTEND_DISCS_NUMBER,
                BCode.B_EXTEND_RUNNING_TIME, BCode.B_EXTEND_LANGUAGES, BCode.B_EXTEND_STAR, BCode.B_EXTEND_DIELECTRIC,
                BCode.B_EXTEND_BRANDS, BCode.B_EXTEND_MATERIAL, BCode.B_EXTEND_PRICE_SCOPE, BCode.B_EXTEND_TRANSLATOR,
                BCode.B_EXTEND_PRICE, BCode.B_EXTEND_SIZE, BCode.B_EXTEND_TIME, BCode.B_EXTEND_BULB,
                BCode.B_EXTEND_ILLUMINANT, BCode.B_EXTEND_BRIGHTNESS, BCode.B_EXTEND_DIMMER, BCode.B_EXTEND_FOCUS,
                BCode.B_EXTEND_RANGE, BCode.B_EXTEND_CHARGING, BCode.B_EXTEND_BATTERY, BCode.B_EXTEND_WATERPROOF,
                BCode.B_EXTEND_EFFICACY, BCode.B_EXTEND_CONSTITUENT, BCode.B_EXTEND_SPECIFICATION,
                BCode.B_EXTEND_CAPACITY, BCode.B_EXTEND_CROWD, BCode.B_EXTEND_NAME, BCode.B_EXTEND_MODEL,
                BCode.B_EXTEND_FACTORY, BCode.B_EXTEND_PRODUCING_AREA, BCode.B_EXTEND_GROSS_WEIGHT,
                BCode.B_EXTEND_WEIGHT, BCode.B_EXTEND_SERIES, BCode.B_EXTEND_CLASSIFICATION,
                BCode.B_EXTEND_CLASSIFICATION_NAME, BCode.B_EXTEND_BOTTLE_CODE, BCode.B_EXTEND_COOPERATION_PUBLISHER,
                BCode.B_EXTEND_CHIEF_EDITOR, BCode.B_EXTEND_EDITOR, BCode.B_EXTEND_FIRST_EDITION,
                BCode.B_EXTEND_PRICING_CURRENCY, BCode.B_EXTEND_CARRIER, BCode.B_EXTEND_SUBJECT_TERM,
                BCode.B_EXTEND_CLC, BCode.B_EXTEND_SUBJECTS, BCode.B_EXTEND_CLASS, BCode.B_EXTEND_DISCIPLINE,
                BCode.B_EXTEND_PUBLISHING_LOCATION, BCode.B_EXTEND_AWARDS, BCode.B_EXTEND_IS_CONPLEX,
                BCode.B_EXTEND_ATTACHMENT, BCode.B_EXTEND_READER, BCode.B_EXTEND_LENGTH, BCode.B_EXTEND_WIDTH,
                BCode.B_EXTEND_THICK, BCode.B_EXTEND_AREA_CODE, BCode.B_EXTEND_DIRECTOR, BCode.B_EXTEND_LYRICS,
                BCode.B_EXTEND_COMPOSE, BCode.B_EXTEND_BAND, BCode.B_EXTEND_MAIN_ACTOR,
                BCode.B_EXTEND_GENERAL_DISTRIBUTION, BCode.B_EXTEND_SUBTITLE, BCode.B_EXTEND_CANON_NAME,
                BCode.B_EXTEND_CANON_NUMBER, BCode.B_EXTEND_ENGLISH_TITLE, BCode.B_EXTEND_VERSION_NAME,
                BCode.B_EXTEND_BAND_NAME, BCode.B_EXTEND_ANNUAL_NUMBER, BCode.B_EXTEND_TIED_SERIES,
                BCode.B_EXTEND_HAS_EPUB, BCode.B_EXTEND_FREE, BCode.B_EXTEND_READ_RANGE,
                BCode.B_EXTEND_EBOOKS_PAGINATION, BCode.B_EXTEND_CIP_DATA, BCode.B_EXTEND_COPYRIGHT_NAME,
                BCode.B_EXTEND_NATIONALITY, BCode.B_EXTEND_DYNASTY, BCode.B_EXTEND_OTHER_AUTHER,
                BCode.B_EXTEND_CUSTOM_COMPLEX, BCode.B_EXTEND_COVER_NAME, BCode.B_EXTEND_AUTHOR,
                BCode.B_EXTEND_RECOMMEND_TYPE, BCode.B_EXTEND_PUBLISHER_IDENTIFIER,
                BCode.B_EXTEND_FOREIGN_NAME,
                BCode.B_EXTEND_TOME_COUNT, BCode.B_EXTEND_AGE_GROUP, BCode.B_EXTEND_GRADE_GROUP,
                BCode.B_EXTEND_PRINTED_SHEET_AMOUNT, BCode.B_EXTEND_PAINTER,
                BCode.B_EXTEND_IS_SOLD_OUT, BCode.B_EXTEND_SAP_DISTRIBUTION_STATUS,
                BCode.B_EXTEND_PUBLICATION_TYPE, BCode.B_EXTEND_NUMTP));
        DESCRIPTION_BCODES = Collections.unmodifiableSet(Sets.newHashSet(BCode.B_DESCRIPTION_EDITOR_RECOMMEND, BCode.B_DESCRIPTION_CONTENT_ABSTRACT,
                BCode.B_DESCRIPTION_DIGEST, BCode.B_DESCRIPTION_DIRECTORY, BCode.B_DESCRIPTION_ABOUT_AUTHOR,
                BCode.B_DESCRIPTION_MEDIA_COMMENTARY, BCode.B_DESCRIPTION_INTRODUCTION, BCode.B_DESCRIPTION_SPECIFICATION,
                BCode.B_DESCRIPTION_PACKING_LIST, BCode.B_DESCRIPTION_AFTERSALES, BCode.B_DESCRIPTION_MEDIA_OLD,
                BCode.B_DESCRIPTION_HIGHLIGHT, BCode.B_DESCRIPTION_CELEBRITY_RECOMMEN, BCode.B_DESCRIPTION_SELLING_PROINT,
                BCode.B_DESCRIPTION_AWARDS));
        PRODUCT_BCODES = Collections.unmodifiableSet(Sets.newHashSet(BCode.B_PRODUCT_MERCH_ID, BCode.B_PRODUCT_AUTHOR, BCode.B_PRODUCT_ISBN, BCode.B_PRODUCT_NAME,
                BCode.B_PRODUCT_LIST_PRICE, BCode.B_PRODUCT_PRODUCTION_DATE, BCode.B_PRODUCT_SORT, BCode.B_PRODUCT_LOCKED,
                BCode.B_PRODUCT_COMPLEX, BCode.B_PRODUCT_HAS_IMG, BCode.B_PRODUCT_MANUFACTURER, BCode.B_PRODUCT_PRINT_DATE,
                BCode.B_PRODUCT_MC));
        PRODUCT_SALE_BCODES = Collections.unmodifiableSet(Sets.newHashSet(BCode.B_PRODUCT_SALE_SHOP, BCode.B_PRODUCT_SALE_STATUS, BCode.B_PRODUCT_SALE_AUDITS_STATUS,
                BCode.B_PRODUCT_SALE_SUPPLY_TYPE, BCode.B_PRODUCT_SALE_STORAGE_TYPE, BCode.B_PRODUCT_SALE_NAME,
                BCode.B_PRODUCT_SALE_OUTER_ID, BCode.B_PRODUCT_SALE_VENDOR, BCode.B_PRODUCT_SALE_BUNDLE,
                BCode.B_PRODUCT_SALE_MANAGE_CATEGORY, BCode.B_PRODUCT_SALE_WORK_CATEGORY, BCode.B_PRODUCT_SALE_SELLING_POINTS,
                BCode.B_PRODUCT_SALE_ID));
    }

    private final Long id;
    private final String code;
    private final String name;
    private final Class clazz;
    private final boolean isMulti;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }


    public Class getClazz() {
        return clazz;
    }

    public boolean isMulti() {
        return isMulti;
    }

    public Long getId() {
        return id;
    }


    BCode(Long id, String code, String name, Class clazz) {
        this(id, code, name, clazz, false);
    }

    BCode(Long id, String code, String name, Class clazz, boolean isMulti) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.clazz = clazz;
        this.isMulti = isMulti;
    }
}
