
    create table account (
        account_id integer not null auto_increment,
        is_active bit,
        created_at datetime(6),
        avatar_path varchar(255),
        email varchar(255),
        full_name varchar(255) not null,
        google_id varchar(255),
        password_hash varchar(255),
        phone varchar(255),
        role enum ('ADMIN','CUSTOMER','OWNER','STAFF') not null,
        primary key (account_id)
    ) engine=InnoDB;

    create table blog_comment (
        author_account_id integer not null,
        comment_id integer not null auto_increment,
        is_deleted bit,
        moderated_by_account_id integer,
        post_id integer not null,
        created_at datetime(6),
        moderated_at datetime(6),
        updated_at datetime(6),
        content varchar(1000) not null,
        status enum ('APPROVED','PENDING','REJECTED') not null,
        primary key (comment_id)
    ) engine=InnoDB;

    create table blog_post (
        author_account_id integer not null,
        is_deleted bit,
        post_id integer not null auto_increment,
        created_at datetime(6),
        published_at datetime(6),
        updated_at datetime(6),
        status varchar(20) not null,
        title varchar(200) not null,
        summary varchar(500),
        content LONGTEXT not null,
        primary key (post_id)
    ) engine=InnoDB;

    create table blog_reaction (
        account_id integer not null,
        post_id integer not null,
        reaction_id integer not null auto_increment,
        created_at datetime(6),
        emoji_code enum ('ANGRY','HEART','LAUGH','LIKE','SAD','WOW') not null,
        primary key (reaction_id)
    ) engine=InnoDB;

    create table booking (
        account_id integer,
        booking_id integer not null auto_increment,
        facility_id integer not null,
        guest_id integer,
        staff_id integer,
        checkin_time datetime(6),
        checkout_time datetime(6),
        created_at datetime(6),
        hold_expired_at datetime(6),
        note varchar(500),
        booking_status enum ('CANCELLED','COMPLETED','CONFIRMED','EXPIRED','PENDING'),
        primary key (booking_id)
    ) engine=InnoDB;

    create table booking_change_log (
        actor_staff_id integer,
        booking_id integer not null,
        change_id integer not null auto_increment,
        new_booking_date date,
        new_court_id integer,
        new_end_time time(0),
        new_start_time time(0),
        old_booking_date date,
        old_court_id integer,
        old_end_time time(0),
        old_start_time time(0),
        refund_due decimal(12,2),
        change_time datetime(6),
        change_type varchar(20),
        change_action varchar(30),
        etag_after varchar(64),
        etag_before varchar(64),
        reason varchar(500),
        after_data LONGTEXT,
        before_data LONGTEXT,
        note varchar(255),
        primary key (change_id)
    ) engine=InnoDB;

    create table booking_slot (
        booking_date date not null,
        booking_id integer not null,
        booking_slot_id integer not null auto_increment,
        court_id integer not null,
        end_time time(0) not null,
        price_snapshot decimal(12,2) not null,
        start_time time(0) not null,
        checkin_time datetime(6),
        checkout_time datetime(6),
        slot_status enum ('CANCELLED','CHECKED_IN','CHECKED_OUT','NO_SHOW','PENDING'),
        primary key (booking_slot_id)
    ) engine=InnoDB;

    create table commission_change_log (
        change_log_id integer not null auto_increment,
        changed_by integer not null,
        max_price_per_minute decimal(12,2),
        min_price_per_minute decimal(12,2) not null,
        new_rate decimal(5,4) not null,
        new_tier_id integer not null,
        notice_days integer not null,
        old_rate decimal(5,4),
        old_tier_id integer,
        announced_at datetime(6) not null,
        created_at datetime(6),
        effective_from datetime(6) not null,
        reason varchar(500),
        change_type enum ('CREATE','EXPIRE','UPDATE') not null,
        primary key (change_log_id)
    ) engine=InnoDB;

    create table commission_policy (
        min_notice_days integer not null,
        policy_id integer not null auto_increment,
        updated_by integer,
        updated_at datetime(6),
        description varchar(500),
        primary key (policy_id)
    ) engine=InnoDB;

    create table commission_tier (
        commission_rate decimal(5,4) not null,
        created_by integer,
        is_current bit not null,
        max_price_per_minute decimal(12,2),
        min_price_per_minute decimal(12,2) not null,
        notice_days integer,
        tier_id integer not null auto_increment,
        announced_at datetime(6),
        created_at datetime(6),
        effective_from datetime(6) not null,
        effective_to datetime(6),
        updated_at datetime(6),
        description varchar(500),
        status enum ('ACTIVE','ANNOUNCED','DRAFT','EXPIRED'),
        primary key (tier_id)
    ) engine=InnoDB;

    create table court (
        court_id integer not null auto_increment,
        facility_sport_id integer not null,
        is_active bit,
        description varchar(500),
        court_name varchar(255) not null,
        primary key (court_id)
    ) engine=InnoDB;

    create table court_attribute_value (
        attribute_id integer not null,
        court_id integer not null,
        id integer not null auto_increment,
        value varchar(255) not null,
        primary key (id)
    ) engine=InnoDB;

    create table court_schedule_exception (
        court_id integer not null,
        created_by integer,
        end_date date not null,
        end_time time(0),
        exception_id integer not null auto_increment,
        facility_id integer not null,
        is_active bit,
        start_date date not null,
        start_time time(0),
        created_at datetime(6),
        updated_at datetime(6),
        exception_type varchar(20),
        reason varchar(300),
        primary key (exception_id)
    ) engine=InnoDB;

    create table court_slot_booking (
        booking_date date not null,
        booking_slot_id integer not null,
        court_id integer not null,
        end_time time(0) not null,
        id integer not null auto_increment,
        start_time time(0) not null,
        primary key (id)
    ) engine=InnoDB;

    create table customer_favorite_facility (
        account_id integer not null,
        facility_id integer not null,
        favorite_id integer not null auto_increment,
        primary key (favorite_id)
    ) engine=InnoDB;

    create table email_queue (
        booking_id integer,
        email_id integer not null auto_increment,
        retry_count integer not null,
        created_at datetime(6) not null,
        next_attempt_at datetime(6) not null,
        reminder_at datetime(6),
        sent_at datetime(6),
        status varchar(20) not null,
        email_type varchar(30) not null,
        last_error varchar(500),
        payload_json LONGTEXT,
        to_email varchar(255) not null,
        primary key (email_id)
    ) engine=InnoDB;

    create table email_verification (
        id integer not null auto_increment,
        created_at datetime(6),
        expire_at datetime(6) not null,
        phone varchar(20),
        email varchar(255) not null,
        full_name varchar(255) not null,
        password_hash varchar(255) not null,
        token varchar(255) not null,
        role enum ('ADMIN','CUSTOMER','OWNER','STAFF') not null,
        primary key (id)
    ) engine=InnoDB;

    create table facility (
        close_time time(0) not null,
        facility_id integer not null auto_increment,
        is_active bit,
        latitude decimal(38,2),
        longitude decimal(38,2),
        open_time time(0) not null,
        owner_account_id integer not null,
        created_at datetime(6),
        address varchar(255) not null,
        description LONGTEXT,
        district varchar(255),
        name varchar(255) not null,
        province varchar(255),
        ward varchar(255),
        approval_status enum ('APPROVED','PENDING','REJECTED'),
        primary key (facility_id)
    ) engine=InnoDB;

    create table facility_image (
        facility_id integer not null,
        image_id integer not null auto_increment,
        is_thumbnail bit,
        created_at datetime(6),
        image_path varchar(255) not null,
        primary key (image_id)
    ) engine=InnoDB;

    create table facility_price_rule (
        effective_from date not null,
        effective_to date,
        end_time time(0) not null,
        facility_sport_id integer not null,
        is_active bit,
        price_per_slot decimal(12,2) not null,
        price_rule_id integer not null auto_increment,
        start_time time(0) not null,
        created_at datetime(6),
        day_type enum ('HOLIDAY','WEEKDAY','WEEKEND') not null,
        primary key (price_rule_id)
    ) engine=InnoDB;

    create table facility_sport (
        facility_id integer not null,
        facility_sport_id integer not null auto_increment,
        is_active bit,
        min_duration_minutes integer not null,
        slot_step_minutes integer not null,
        sport_id integer not null,
        primary key (facility_sport_id)
    ) engine=InnoDB;

    create table guest (
        guest_id integer not null auto_increment,
        email varchar(255),
        guest_name varchar(255) not null,
        phone varchar(255) not null,
        primary key (guest_id)
    ) engine=InnoDB;

    create table invoice (
        booking_id integer not null,
        court_amount decimal(12,2) not null,
        deposit_percent integer,
        discount_amount decimal(12,2) not null,
        invoice_id integer not null auto_increment,
        paid_amount decimal(12,2),
        product_amount decimal(12,2) not null,
        refund_due decimal(12,2) not null,
        subtotal decimal(12,2) not null,
        total_amount decimal(12,2) not null,
        voucher_id integer,
        created_at datetime(6),
        refund_note varchar(500),
        payment_status enum ('PAID','PARTIAL','UNPAID'),
        refund_status enum ('NONE','PENDING_MANUAL','REFUNDED'),
        primary key (invoice_id)
    ) engine=InnoDB;

    create table notification (
        account_id integer not null,
        is_read bit,
        is_sent bit,
        notification_id integer not null auto_increment,
        created_at datetime(6),
        type varchar(10),
        content varchar(500),
        title varchar(255),
        primary key (notification_id)
    ) engine=InnoDB;

    create table order_item (
        booking_id integer not null,
        order_item_id integer not null auto_increment,
        product_id integer not null,
        quantity integer not null,
        rental_duration integer,
        total_amount decimal(12,2) not null,
        unit_price_snapshot decimal(12,2) not null,
        created_at datetime(6),
        added_by varchar(10),
        primary key (order_item_id)
    ) engine=InnoDB;

    create table owner_profile (
        account_id integer not null,
        approved_by integer,
        owner_profile_id integer not null auto_increment,
        approved_at datetime(6),
        created_at datetime(6),
        bank_account_name varchar(255),
        bank_account_no varchar(255),
        bank_name varchar(255),
        business_name varchar(255) not null,
        tax_code varchar(255),
        approval_status enum ('APPROVED','PENDING','REJECTED'),
        primary key (owner_profile_id)
    ) engine=InnoDB;

    create table password_reset_token (
        id integer not null auto_increment,
        created_at datetime(6) not null,
        expire_at datetime(6) not null,
        email varchar(255) not null,
        token varchar(255) not null,
        primary key (id)
    ) engine=InnoDB;

    create table payment (
        invoice_id integer not null,
        paid_amount decimal(12,2),
        payment_id integer not null auto_increment,
        staff_confirm_id integer,
        confirm_time datetime(6),
        created_at datetime(6),
        expire_at datetime(6),
        payment_time datetime(6),
        vnpay_response_code varchar(10),
        transaction_code varchar(100),
        vnpay_txn_no varchar(100),
        method enum ('BANK_TRANSFER','CASH','VNPAY'),
        payment_status enum ('FAILED','PENDING','SUCCESS'),
        payment_type enum ('DEPOSIT','FULL','REMAINING'),
        primary key (payment_id)
    ) engine=InnoDB;

    create table platform_commission (
        commission_amount decimal(12,2) not null,
        commission_id integer not null auto_increment,
        commission_rate decimal(5,4) not null,
        commission_tier_id integer,
        court_revenue decimal(12,2) not null,
        invoice_id integer not null,
        owner_account_id integer not null,
        owner_payout decimal(12,2) not null,
        voucher_cost_owner decimal(12,2),
        voucher_cost_platform decimal(12,2),
        created_at datetime(6),
        settled_at datetime(6),
        status enum ('FAILED','PENDING','SETTLED'),
        primary key (commission_id)
    ) engine=InnoDB;

    create table product (
        category_id integer not null,
        facility_id integer not null,
        is_active bit,
        price decimal(12,2) not null,
        product_id integer not null auto_increment,
        stock_quantity integer,
        created_at datetime(6),
        rental_unit varchar(20),
        description varchar(500),
        image_path varchar(255),
        product_name varchar(255) not null,
        product_type enum ('RENTAL','SALE') not null,
        primary key (product_id)
    ) engine=InnoDB;

    create table product_category (
        category_id integer not null auto_increment,
        is_active bit,
        category_code varchar(30) not null,
        category_name varchar(255) not null,
        primary key (category_id)
    ) engine=InnoDB;

    create table review (
        account_id integer not null,
        booking_id integer not null,
        rating integer not null,
        review_id integer not null auto_increment,
        created_at datetime(6),
        comment varchar(500),
        primary key (review_id)
    ) engine=InnoDB;

    create table sport (
        default_min_duration_minutes integer not null,
        default_slot_step_minutes integer not null,
        is_active bit,
        sport_id integer not null auto_increment,
        sport_code varchar(30) not null,
        icon_path varchar(255),
        sport_name varchar(255) not null,
        primary key (sport_id)
    ) engine=InnoDB;

    create table sport_attribute (
        attribute_id integer not null auto_increment,
        is_required bit,
        sport_id integer not null,
        data_type varchar(20) not null,
        attribute_code varchar(50) not null,
        attribute_name varchar(255) not null,
        options_json LONGTEXT,
        primary key (attribute_id)
    ) engine=InnoDB;

    create table staff (
        account_id integer not null,
        facility_id integer not null,
        is_active bit,
        staff_id integer not null auto_increment,
        primary key (staff_id)
    ) engine=InnoDB;

    create table voucher (
        discount_value decimal(12,2) not null,
        is_active bit,
        issuer_account_id integer,
        max_discount_amount decimal(12,2),
        min_order_amount decimal(12,2),
        per_user_limit integer,
        usage_limit integer,
        voucher_id integer not null auto_increment,
        created_at datetime(6),
        valid_from datetime(6) not null,
        valid_to datetime(6) not null,
        code varchar(50) not null,
        description varchar(500),
        name varchar(255) not null,
        applicable_to enum ('ALL','COURT_BOOKING','PRODUCT'),
        discount_type enum ('FIXED_AMOUNT','PERCENTAGE') not null,
        issuer_type enum ('OWNER','PLATFORM') not null,
        primary key (voucher_id)
    ) engine=InnoDB;

    create table voucher_account (
        account_id integer not null,
        voucher_id integer not null
    ) engine=InnoDB;

    create table voucher_facility (
        facility_id integer not null,
        voucher_id integer not null
    ) engine=InnoDB;

    create table voucher_usage (
        account_id integer,
        booking_id integer not null,
        discount_amount decimal(12,2) not null,
        invoice_id integer not null,
        usage_id integer not null auto_increment,
        voucher_id integer not null,
        used_at datetime(6),
        liability_party varchar(10) not null,
        primary key (usage_id)
    ) engine=InnoDB;

    alter table account 
       add constraint UKq0uja26qgu1atulenwup9rxyr unique (email);

    alter table account 
       add constraint UKdgdnj692f2g5ebicy1xyc2l3w unique (phone);

    alter table blog_reaction 
       add constraint UQ_BlogReaction unique (post_id, account_id, emoji_code);

    alter table court_attribute_value 
       add constraint UKj4qa3uj33gp8awnx64rtgrn9r unique (court_id, attribute_id);

    alter table court_slot_booking 
       add constraint UKlwv7d7t524r3bxmi0h523xs9p unique (court_id, booking_date, start_time);

    alter table court_slot_booking 
       add constraint UKceeulgl6uatuliycmothuay41 unique (booking_slot_id);

    alter table customer_favorite_facility 
       add constraint UKq90nef112fe35x5g3ad9042u7 unique (account_id, facility_id);

    alter table facility_sport 
       add constraint UKdxig1mrlph0g5uqke1em81bl4 unique (facility_id, sport_id);

    alter table invoice 
       add constraint UK32ywtxrkeu1wnmivu6mlcqdid unique (booking_id);

    alter table owner_profile 
       add constraint UKhnrcmp4lyxkm7l0p5uegwmw8j unique (account_id);

    alter table password_reset_token 
       add constraint UKg0guo4k8krgpwuagos61oc06j unique (token);

    alter table platform_commission 
       add constraint UKc4yic37b8li7uaejl34er5xkd unique (invoice_id);

    alter table product_category 
       add constraint UKn4nmm8o4cegxl3lt9p48mklvj unique (category_code);

    alter table review 
       add constraint UKm685o801uf70i84jf94qq3d0b unique (booking_id);

    alter table sport 
       add constraint UK91qpqfcq5rjyanobl8fkxsch1 unique (sport_code);

    alter table staff 
       add constraint UK4uqyb8awsv3mfncjj737o7oo9 unique (account_id);

    alter table voucher 
       add constraint UKpvh1lqheshnjoekevvwla03xn unique (code);

    alter table blog_comment 
       add constraint FKtrmc5ogfx7w1u0u5uxo2nl93f 
       foreign key (author_account_id) 
       references account (account_id);

    alter table blog_comment 
       add constraint FKmbe5fcwvss5qwqlj6o4vpiwvl 
       foreign key (moderated_by_account_id) 
       references account (account_id);

    alter table blog_comment 
       add constraint FKeh1bvld0i4iq1rnw951g518l8 
       foreign key (post_id) 
       references blog_post (post_id);

    alter table blog_post 
       add constraint FK1wbk80unrcd3nis0hiwwfuwxu 
       foreign key (author_account_id) 
       references account (account_id);

    alter table blog_reaction 
       add constraint FKdh1rphxlewbgndyi4t8jw9w8j 
       foreign key (account_id) 
       references account (account_id);

    alter table blog_reaction 
       add constraint FKpxoeu71558cb4xvrhoj3g8biq 
       foreign key (post_id) 
       references blog_post (post_id);

    alter table booking 
       add constraint FK7hunottedmjhtdcvhv4sx6x4a 
       foreign key (account_id) 
       references account (account_id);

    alter table booking 
       add constraint FK6io8j4ov8vlpwc9wc37179ca1 
       foreign key (facility_id) 
       references facility (facility_id);

    alter table booking 
       add constraint FKjn3lsroa8t8h7x5sld9b0ru2u 
       foreign key (guest_id) 
       references guest (guest_id);

    alter table booking 
       add constraint FK357w452pgne0tsl2dra6ompmx 
       foreign key (staff_id) 
       references staff (staff_id);

    alter table booking_change_log 
       add constraint FKcnnm4t7shy0wf77mkjlwdgrih 
       foreign key (actor_staff_id) 
       references staff (staff_id);

    alter table booking_change_log 
       add constraint FKj14lvcomgp7lablhbkx2f7hrp 
       foreign key (booking_id) 
       references booking (booking_id);

    alter table booking_change_log 
       add constraint FK9a54ebbesyn4vthc8k8nkjm22 
       foreign key (new_court_id) 
       references court (court_id);

    alter table booking_change_log 
       add constraint FKgbqo4nhh83vqqd5dshffu19ow 
       foreign key (old_court_id) 
       references court (court_id);

    alter table booking_slot 
       add constraint FKq5d98cx093epox9u3spe2jcxw 
       foreign key (booking_id) 
       references booking (booking_id);

    alter table booking_slot 
       add constraint FK6k10g0gdoelgwmlr9gvm8otgn 
       foreign key (court_id) 
       references court (court_id);

    alter table commission_change_log 
       add constraint FKrxygn6fv1emlcpjoucw164x6n 
       foreign key (changed_by) 
       references account (account_id);

    alter table commission_change_log 
       add constraint FKhgv1p789veqk3jkudvn26jn1x 
       foreign key (new_tier_id) 
       references commission_tier (tier_id);

    alter table commission_change_log 
       add constraint FKn3poqalsmvccdgvrhivwp4dh0 
       foreign key (old_tier_id) 
       references commission_tier (tier_id);

    alter table commission_policy 
       add constraint FKo6ixex39svs4ahh6atgs08ri3 
       foreign key (updated_by) 
       references account (account_id);

    alter table commission_tier 
       add constraint FKqjmk5flqsu2y0qdv80m2ayfhm 
       foreign key (created_by) 
       references account (account_id);

    alter table court 
       add constraint FKnp646tyaa8dlfc6vgigqakww2 
       foreign key (facility_sport_id) 
       references facility_sport (facility_sport_id);

    alter table court_attribute_value 
       add constraint FKo97x0o4wlmdqn57mbavww72nh 
       foreign key (attribute_id) 
       references sport_attribute (attribute_id);

    alter table court_attribute_value 
       add constraint FKqhof5sbc0wnqlotpcqtu0esxm 
       foreign key (court_id) 
       references court (court_id);

    alter table court_schedule_exception 
       add constraint FKhqusfl723oy0nrnrk03yjpkmq 
       foreign key (court_id) 
       references court (court_id);

    alter table court_schedule_exception 
       add constraint FK9qo8kolyo4fa6ux02vxiuawo0 
       foreign key (created_by) 
       references staff (staff_id);

    alter table court_schedule_exception 
       add constraint FKl22or8cxo4a36qs25unwlkhow 
       foreign key (facility_id) 
       references facility (facility_id);

    alter table court_slot_booking 
       add constraint FK24qfggdwxfwy3vx1qcibdxpod 
       foreign key (booking_slot_id) 
       references booking_slot (booking_slot_id);

    alter table court_slot_booking 
       add constraint FKr1ilo3uku5yxc7s2ys89gt3ql 
       foreign key (court_id) 
       references court (court_id);

    alter table customer_favorite_facility 
       add constraint FKfjrnc2rrq1gnh2oc58lg23xle 
       foreign key (account_id) 
       references account (account_id);

    alter table customer_favorite_facility 
       add constraint FKk7fc6jc0vsrns8m5bltar3sya 
       foreign key (facility_id) 
       references facility (facility_id);

    alter table email_queue 
       add constraint FK97k0psx5jo5nsp5wbwq6u27uv 
       foreign key (booking_id) 
       references booking (booking_id);

    alter table facility 
       add constraint FKdn4v673vwnsce108nyf77pdfa 
       foreign key (owner_account_id) 
       references account (account_id);

    alter table facility_image 
       add constraint FKnrx8tu5s53v5tutiyv2sxw01d 
       foreign key (facility_id) 
       references facility (facility_id);

    alter table facility_price_rule 
       add constraint FKkgdipn6ecv56ew5beggx57yyo 
       foreign key (facility_sport_id) 
       references facility_sport (facility_sport_id);

    alter table facility_sport 
       add constraint FKhjl2x22yxxchd7a04o4tphvx9 
       foreign key (facility_id) 
       references facility (facility_id);

    alter table facility_sport 
       add constraint FK1t4y83xcd33kr3pj0f9nju7m6 
       foreign key (sport_id) 
       references sport (sport_id);

    alter table invoice 
       add constraint FK4jd6uuk7w0d72riyre2w14fl7 
       foreign key (booking_id) 
       references booking (booking_id);

    alter table invoice 
       add constraint FKh8mc37lrohbk7stgatwwn5doq 
       foreign key (voucher_id) 
       references voucher (voucher_id);

    alter table notification 
       add constraint FKj0b1ncedmpl7sx7t7o54t26v2 
       foreign key (account_id) 
       references account (account_id);

    alter table order_item 
       add constraint FKselo28ymemephorc1otqe7lcq 
       foreign key (booking_id) 
       references booking (booking_id);

    alter table order_item 
       add constraint FK551losx9j75ss5d6bfsqvijna 
       foreign key (product_id) 
       references product (product_id);

    alter table owner_profile 
       add constraint FKfjr685c3fuyy9iil9jnrd5v5e 
       foreign key (account_id) 
       references account (account_id);

    alter table owner_profile 
       add constraint FKj879nrm2n76uodcldu5a3jdbk 
       foreign key (approved_by) 
       references account (account_id);

    alter table payment 
       add constraint FKsb24p8f52refbb80qwp4gem9n 
       foreign key (invoice_id) 
       references invoice (invoice_id);

    alter table payment 
       add constraint FKa1s5fly67g3hxiaqa4cylyj9m 
       foreign key (staff_confirm_id) 
       references staff (staff_id);

    alter table platform_commission 
       add constraint FKbxouaeg0eaffbye01ij57255f 
       foreign key (commission_tier_id) 
       references commission_tier (tier_id);

    alter table platform_commission 
       add constraint FKlh81n7x1pc5yye31wgihvqmjl 
       foreign key (invoice_id) 
       references invoice (invoice_id);

    alter table platform_commission 
       add constraint FKdf5r0uiasavnupx2408d1be7m 
       foreign key (owner_account_id) 
       references account (account_id);

    alter table product 
       add constraint FK5cypb0k23bovo3rn1a5jqs6j4 
       foreign key (category_id) 
       references product_category (category_id);

    alter table product 
       add constraint FKlm02uo7su1hxcyflt4goj66ft 
       foreign key (facility_id) 
       references facility (facility_id);

    alter table review 
       add constraint FKbopwpfvcg5qsfrjtt9svofxg1 
       foreign key (account_id) 
       references account (account_id);

    alter table review 
       add constraint FKk4xawqohtguy5yx5nnpba6yf3 
       foreign key (booking_id) 
       references booking (booking_id);

    alter table sport_attribute 
       add constraint FKk5e7n7xud4b8r3mpb1txnvair 
       foreign key (sport_id) 
       references sport (sport_id);

    alter table staff 
       add constraint FKs9jl798sgmtrl79dm4svocvaw 
       foreign key (account_id) 
       references account (account_id);

    alter table staff 
       add constraint FKl0j7hmn56i24kta6sihk6yt7 
       foreign key (facility_id) 
       references facility (facility_id);

    alter table voucher 
       add constraint FK725xijc1ux79ti07v7e4imncx 
       foreign key (issuer_account_id) 
       references account (account_id);

    alter table voucher_account 
       add constraint FKhiiiwumq9tm0nch1hbyxn8lih 
       foreign key (account_id) 
       references account (account_id);

    alter table voucher_account 
       add constraint FK9623iyj2s1sa228y4iq9t8yjm 
       foreign key (voucher_id) 
       references voucher (voucher_id);

    alter table voucher_facility 
       add constraint FKtfu138wbm7w4vj2rx8gj9ie1b 
       foreign key (facility_id) 
       references facility (facility_id);

    alter table voucher_facility 
       add constraint FKoces5ulf2787risi4335clo2r 
       foreign key (voucher_id) 
       references voucher (voucher_id);

    alter table voucher_usage 
       add constraint FKg6fotlb4b4wakjghl8hm4mmma 
       foreign key (account_id) 
       references account (account_id);

    alter table voucher_usage 
       add constraint FKeiom45vjfhkw6yq2jwe4qiigo 
       foreign key (booking_id) 
       references booking (booking_id);

    alter table voucher_usage 
       add constraint FKc0wr3a0vbrw2wk4c5wk3ld0o1 
       foreign key (invoice_id) 
       references invoice (invoice_id);

    alter table voucher_usage 
       add constraint FKbrrttecc2rpi70ouyw9rjrv1i 
       foreign key (voucher_id) 
       references voucher (voucher_id);

    create table account (
        account_id integer not null auto_increment,
        is_active bit,
        created_at datetime(6),
        avatar_path varchar(255),
        email varchar(255),
        full_name varchar(255) not null,
        google_id varchar(255),
        password_hash varchar(255),
        phone varchar(255),
        role enum ('ADMIN','CUSTOMER','OWNER','STAFF') not null,
        primary key (account_id)
    ) engine=InnoDB;

    create table blog_comment (
        author_account_id integer not null,
        comment_id integer not null auto_increment,
        is_deleted bit,
        moderated_by_account_id integer,
        post_id integer not null,
        created_at datetime(6),
        moderated_at datetime(6),
        updated_at datetime(6),
        content varchar(1000) not null,
        status enum ('APPROVED','PENDING','REJECTED') not null,
        primary key (comment_id)
    ) engine=InnoDB;

    create table blog_post (
        author_account_id integer not null,
        is_deleted bit,
        post_id integer not null auto_increment,
        created_at datetime(6),
        published_at datetime(6),
        updated_at datetime(6),
        status varchar(20) not null,
        title varchar(200) not null,
        summary varchar(500),
        content LONGTEXT not null,
        primary key (post_id)
    ) engine=InnoDB;

    create table blog_reaction (
        account_id integer not null,
        post_id integer not null,
        reaction_id integer not null auto_increment,
        created_at datetime(6),
        emoji_code enum ('ANGRY','HEART','LAUGH','LIKE','SAD','WOW') not null,
        primary key (reaction_id)
    ) engine=InnoDB;

    create table booking (
        account_id integer,
        booking_id integer not null auto_increment,
        facility_id integer not null,
        guest_id integer,
        staff_id integer,
        checkin_time datetime(6),
        checkout_time datetime(6),
        created_at datetime(6),
        hold_expired_at datetime(6),
        note varchar(500),
        booking_status enum ('CANCELLED','COMPLETED','CONFIRMED','EXPIRED','PENDING'),
        primary key (booking_id)
    ) engine=InnoDB;

    create table booking_change_log (
        actor_staff_id integer,
        booking_id integer not null,
        change_id integer not null auto_increment,
        new_booking_date date,
        new_court_id integer,
        new_end_time time(0),
        new_start_time time(0),
        old_booking_date date,
        old_court_id integer,
        old_end_time time(0),
        old_start_time time(0),
        refund_due decimal(12,2),
        change_time datetime(6),
        change_type varchar(20),
        change_action varchar(30),
        etag_after varchar(64),
        etag_before varchar(64),
        reason varchar(500),
        after_data LONGTEXT,
        before_data LONGTEXT,
        note varchar(255),
        primary key (change_id)
    ) engine=InnoDB;

    create table booking_slot (
        booking_date date not null,
        booking_id integer not null,
        booking_slot_id integer not null auto_increment,
        court_id integer not null,
        end_time time(0) not null,
        price_snapshot decimal(12,2) not null,
        start_time time(0) not null,
        checkin_time datetime(6),
        checkout_time datetime(6),
        slot_status enum ('CANCELLED','CHECKED_IN','CHECKED_OUT','NO_SHOW','PENDING'),
        primary key (booking_slot_id)
    ) engine=InnoDB;

    create table commission_change_log (
        change_log_id integer not null auto_increment,
        changed_by integer not null,
        max_price_per_minute decimal(12,2),
        min_price_per_minute decimal(12,2) not null,
        new_rate decimal(5,4) not null,
        new_tier_id integer not null,
        notice_days integer not null,
        old_rate decimal(5,4),
        old_tier_id integer,
        announced_at datetime(6) not null,
        created_at datetime(6),
        effective_from datetime(6) not null,
        reason varchar(500),
        change_type enum ('CREATE','EXPIRE','UPDATE') not null,
        primary key (change_log_id)
    ) engine=InnoDB;

    create table commission_policy (
        min_notice_days integer not null,
        policy_id integer not null auto_increment,
        updated_by integer,
        updated_at datetime(6),
        description varchar(500),
        primary key (policy_id)
    ) engine=InnoDB;

    create table commission_tier (
        commission_rate decimal(5,4) not null,
        created_by integer,
        is_current bit not null,
        max_price_per_minute decimal(12,2),
        min_price_per_minute decimal(12,2) not null,
        notice_days integer,
        tier_id integer not null auto_increment,
        announced_at datetime(6),
        created_at datetime(6),
        effective_from datetime(6) not null,
        effective_to datetime(6),
        updated_at datetime(6),
        description varchar(500),
        status enum ('ACTIVE','ANNOUNCED','DRAFT','EXPIRED'),
        primary key (tier_id)
    ) engine=InnoDB;

    create table court (
        court_id integer not null auto_increment,
        facility_sport_id integer not null,
        is_active bit,
        description varchar(500),
        court_name varchar(255) not null,
        primary key (court_id)
    ) engine=InnoDB;

    create table court_attribute_value (
        attribute_id integer not null,
        court_id integer not null,
        id integer not null auto_increment,
        value varchar(255) not null,
        primary key (id)
    ) engine=InnoDB;

    create table court_schedule_exception (
        court_id integer not null,
        created_by integer,
        end_date date not null,
        end_time time(0),
        exception_id integer not null auto_increment,
        facility_id integer not null,
        is_active bit,
        start_date date not null,
        start_time time(0),
        created_at datetime(6),
        updated_at datetime(6),
        exception_type varchar(20),
        reason varchar(300),
        primary key (exception_id)
    ) engine=InnoDB;

    create table court_slot_booking (
        booking_date date not null,
        booking_slot_id integer not null,
        court_id integer not null,
        end_time time(0) not null,
        id integer not null auto_increment,
        start_time time(0) not null,
        primary key (id)
    ) engine=InnoDB;

    create table customer_favorite_facility (
        account_id integer not null,
        facility_id integer not null,
        favorite_id integer not null auto_increment,
        primary key (favorite_id)
    ) engine=InnoDB;

    create table email_queue (
        booking_id integer,
        email_id integer not null auto_increment,
        retry_count integer not null,
        created_at datetime(6) not null,
        next_attempt_at datetime(6) not null,
        reminder_at datetime(6),
        sent_at datetime(6),
        status varchar(20) not null,
        email_type varchar(30) not null,
        last_error varchar(500),
        payload_json LONGTEXT,
        to_email varchar(255) not null,
        primary key (email_id)
    ) engine=InnoDB;

    create table email_verification (
        id integer not null auto_increment,
        created_at datetime(6),
        expire_at datetime(6) not null,
        phone varchar(20),
        email varchar(255) not null,
        full_name varchar(255) not null,
        password_hash varchar(255) not null,
        token varchar(255) not null,
        role enum ('ADMIN','CUSTOMER','OWNER','STAFF') not null,
        primary key (id)
    ) engine=InnoDB;

    create table facility (
        close_time time(0) not null,
        facility_id integer not null auto_increment,
        is_active bit,
        latitude decimal(38,2),
        longitude decimal(38,2),
        open_time time(0) not null,
        owner_account_id integer not null,
        created_at datetime(6),
        address varchar(255) not null,
        description LONGTEXT,
        district varchar(255),
        name varchar(255) not null,
        province varchar(255),
        ward varchar(255),
        approval_status enum ('APPROVED','PENDING','REJECTED'),
        primary key (facility_id)
    ) engine=InnoDB;

    create table facility_image (
        facility_id integer not null,
        image_id integer not null auto_increment,
        is_thumbnail bit,
        created_at datetime(6),
        image_path varchar(255) not null,
        primary key (image_id)
    ) engine=InnoDB;

    create table facility_price_rule (
        effective_from date not null,
        effective_to date,
        end_time time(0) not null,
        facility_sport_id integer not null,
        is_active bit,
        price_per_slot decimal(12,2) not null,
        price_rule_id integer not null auto_increment,
        start_time time(0) not null,
        created_at datetime(6),
        day_type enum ('HOLIDAY','WEEKDAY','WEEKEND') not null,
        primary key (price_rule_id)
    ) engine=InnoDB;

    create table facility_sport (
        facility_id integer not null,
        facility_sport_id integer not null auto_increment,
        is_active bit,
        min_duration_minutes integer not null,
        slot_step_minutes integer not null,
        sport_id integer not null,
        primary key (facility_sport_id)
    ) engine=InnoDB;

    create table guest (
        guest_id integer not null auto_increment,
        email varchar(255),
        guest_name varchar(255) not null,
        phone varchar(255) not null,
        primary key (guest_id)
    ) engine=InnoDB;

    create table invoice (
        booking_id integer not null,
        court_amount decimal(12,2) not null,
        deposit_percent integer,
        discount_amount decimal(12,2) not null,
        invoice_id integer not null auto_increment,
        paid_amount decimal(12,2),
        product_amount decimal(12,2) not null,
        refund_due decimal(12,2) not null,
        subtotal decimal(12,2) not null,
        total_amount decimal(12,2) not null,
        voucher_id integer,
        created_at datetime(6),
        refund_note varchar(500),
        payment_status enum ('PAID','PARTIAL','UNPAID'),
        refund_status enum ('NONE','PENDING_MANUAL','REFUNDED'),
        primary key (invoice_id)
    ) engine=InnoDB;

    create table notification (
        account_id integer not null,
        is_read bit,
        is_sent bit,
        notification_id integer not null auto_increment,
        created_at datetime(6),
        type varchar(10),
        content varchar(500),
        title varchar(255),
        primary key (notification_id)
    ) engine=InnoDB;

    create table order_item (
        booking_id integer not null,
        order_item_id integer not null auto_increment,
        product_id integer not null,
        quantity integer not null,
        rental_duration integer,
        total_amount decimal(12,2) not null,
        unit_price_snapshot decimal(12,2) not null,
        created_at datetime(6),
        added_by varchar(10),
        primary key (order_item_id)
    ) engine=InnoDB;

    create table owner_profile (
        account_id integer not null,
        approved_by integer,
        owner_profile_id integer not null auto_increment,
        approved_at datetime(6),
        created_at datetime(6),
        bank_account_name varchar(255),
        bank_account_no varchar(255),
        bank_name varchar(255),
        business_name varchar(255) not null,
        tax_code varchar(255),
        approval_status enum ('APPROVED','PENDING','REJECTED'),
        primary key (owner_profile_id)
    ) engine=InnoDB;

    create table password_reset_token (
        id integer not null auto_increment,
        created_at datetime(6) not null,
        expire_at datetime(6) not null,
        email varchar(255) not null,
        token varchar(255) not null,
        primary key (id)
    ) engine=InnoDB;

    create table payment (
        invoice_id integer not null,
        paid_amount decimal(12,2),
        payment_id integer not null auto_increment,
        staff_confirm_id integer,
        confirm_time datetime(6),
        created_at datetime(6),
        expire_at datetime(6),
        payment_time datetime(6),
        vnpay_response_code varchar(10),
        transaction_code varchar(100),
        vnpay_txn_no varchar(100),
        method enum ('BANK_TRANSFER','CASH','VNPAY'),
        payment_status enum ('FAILED','PENDING','SUCCESS'),
        payment_type enum ('DEPOSIT','FULL','REMAINING'),
        primary key (payment_id)
    ) engine=InnoDB;

    create table platform_commission (
        commission_amount decimal(12,2) not null,
        commission_id integer not null auto_increment,
        commission_rate decimal(5,4) not null,
        commission_tier_id integer,
        court_revenue decimal(12,2) not null,
        invoice_id integer not null,
        owner_account_id integer not null,
        owner_payout decimal(12,2) not null,
        voucher_cost_owner decimal(12,2),
        voucher_cost_platform decimal(12,2),
        created_at datetime(6),
        settled_at datetime(6),
        status enum ('FAILED','PENDING','SETTLED'),
        primary key (commission_id)
    ) engine=InnoDB;

    create table product (
        category_id integer not null,
        facility_id integer not null,
        is_active bit,
        price decimal(12,2) not null,
        product_id integer not null auto_increment,
        stock_quantity integer,
        created_at datetime(6),
        rental_unit varchar(20),
        description varchar(500),
        image_path varchar(255),
        product_name varchar(255) not null,
        product_type enum ('RENTAL','SALE') not null,
        primary key (product_id)
    ) engine=InnoDB;

    create table product_category (
        category_id integer not null auto_increment,
        is_active bit,
        category_code varchar(30) not null,
        category_name varchar(255) not null,
        primary key (category_id)
    ) engine=InnoDB;

    create table review (
        account_id integer not null,
        booking_id integer not null,
        rating integer not null,
        review_id integer not null auto_increment,
        created_at datetime(6),
        comment varchar(500),
        primary key (review_id)
    ) engine=InnoDB;

    create table sport (
        default_min_duration_minutes integer not null,
        default_slot_step_minutes integer not null,
        is_active bit,
        sport_id integer not null auto_increment,
        sport_code varchar(30) not null,
        icon_path varchar(255),
        sport_name varchar(255) not null,
        primary key (sport_id)
    ) engine=InnoDB;

    create table sport_attribute (
        attribute_id integer not null auto_increment,
        is_required bit,
        sport_id integer not null,
        data_type varchar(20) not null,
        attribute_code varchar(50) not null,
        attribute_name varchar(255) not null,
        options_json LONGTEXT,
        primary key (attribute_id)
    ) engine=InnoDB;

    create table staff (
        account_id integer not null,
        facility_id integer not null,
        is_active bit,
        staff_id integer not null auto_increment,
        primary key (staff_id)
    ) engine=InnoDB;

    create table voucher (
        discount_value decimal(12,2) not null,
        is_active bit,
        issuer_account_id integer,
        max_discount_amount decimal(12,2),
        min_order_amount decimal(12,2),
        per_user_limit integer,
        usage_limit integer,
        voucher_id integer not null auto_increment,
        created_at datetime(6),
        valid_from datetime(6) not null,
        valid_to datetime(6) not null,
        code varchar(50) not null,
        description varchar(500),
        name varchar(255) not null,
        applicable_to enum ('ALL','COURT_BOOKING','PRODUCT'),
        discount_type enum ('FIXED_AMOUNT','PERCENTAGE') not null,
        issuer_type enum ('OWNER','PLATFORM') not null,
        primary key (voucher_id)
    ) engine=InnoDB;

    create table voucher_account (
        account_id integer not null,
        voucher_id integer not null
    ) engine=InnoDB;

    create table voucher_facility (
        facility_id integer not null,
        voucher_id integer not null
    ) engine=InnoDB;

    create table voucher_usage (
        account_id integer,
        booking_id integer not null,
        discount_amount decimal(12,2) not null,
        invoice_id integer not null,
        usage_id integer not null auto_increment,
        voucher_id integer not null,
        used_at datetime(6),
        liability_party varchar(10) not null,
        primary key (usage_id)
    ) engine=InnoDB;

    alter table account 
       add constraint UKq0uja26qgu1atulenwup9rxyr unique (email);

    alter table account 
       add constraint UKdgdnj692f2g5ebicy1xyc2l3w unique (phone);

    alter table blog_reaction 
       add constraint UQ_BlogReaction unique (post_id, account_id, emoji_code);

    alter table court_attribute_value 
       add constraint UKj4qa3uj33gp8awnx64rtgrn9r unique (court_id, attribute_id);

    alter table court_slot_booking 
       add constraint UKlwv7d7t524r3bxmi0h523xs9p unique (court_id, booking_date, start_time);

    alter table court_slot_booking 
       add constraint UKceeulgl6uatuliycmothuay41 unique (booking_slot_id);

    alter table customer_favorite_facility 
       add constraint UKq90nef112fe35x5g3ad9042u7 unique (account_id, facility_id);

    alter table facility_sport 
       add constraint UKdxig1mrlph0g5uqke1em81bl4 unique (facility_id, sport_id);

    alter table invoice 
       add constraint UK32ywtxrkeu1wnmivu6mlcqdid unique (booking_id);

    alter table owner_profile 
       add constraint UKhnrcmp4lyxkm7l0p5uegwmw8j unique (account_id);

    alter table password_reset_token 
       add constraint UKg0guo4k8krgpwuagos61oc06j unique (token);

    alter table platform_commission 
       add constraint UKc4yic37b8li7uaejl34er5xkd unique (invoice_id);

    alter table product_category 
       add constraint UKn4nmm8o4cegxl3lt9p48mklvj unique (category_code);

    alter table review 
       add constraint UKm685o801uf70i84jf94qq3d0b unique (booking_id);

    alter table sport 
       add constraint UK91qpqfcq5rjyanobl8fkxsch1 unique (sport_code);

    alter table staff 
       add constraint UK4uqyb8awsv3mfncjj737o7oo9 unique (account_id);

    alter table voucher 
       add constraint UKpvh1lqheshnjoekevvwla03xn unique (code);

    alter table blog_comment 
       add constraint FKtrmc5ogfx7w1u0u5uxo2nl93f 
       foreign key (author_account_id) 
       references account (account_id);

    alter table blog_comment 
       add constraint FKmbe5fcwvss5qwqlj6o4vpiwvl 
       foreign key (moderated_by_account_id) 
       references account (account_id);

    alter table blog_comment 
       add constraint FKeh1bvld0i4iq1rnw951g518l8 
       foreign key (post_id) 
       references blog_post (post_id);

    alter table blog_post 
       add constraint FK1wbk80unrcd3nis0hiwwfuwxu 
       foreign key (author_account_id) 
       references account (account_id);

    alter table blog_reaction 
       add constraint FKdh1rphxlewbgndyi4t8jw9w8j 
       foreign key (account_id) 
       references account (account_id);

    alter table blog_reaction 
       add constraint FKpxoeu71558cb4xvrhoj3g8biq 
       foreign key (post_id) 
       references blog_post (post_id);

    alter table booking 
       add constraint FK7hunottedmjhtdcvhv4sx6x4a 
       foreign key (account_id) 
       references account (account_id);

    alter table booking 
       add constraint FK6io8j4ov8vlpwc9wc37179ca1 
       foreign key (facility_id) 
       references facility (facility_id);

    alter table booking 
       add constraint FKjn3lsroa8t8h7x5sld9b0ru2u 
       foreign key (guest_id) 
       references guest (guest_id);

    alter table booking 
       add constraint FK357w452pgne0tsl2dra6ompmx 
       foreign key (staff_id) 
       references staff (staff_id);

    alter table booking_change_log 
       add constraint FKcnnm4t7shy0wf77mkjlwdgrih 
       foreign key (actor_staff_id) 
       references staff (staff_id);

    alter table booking_change_log 
       add constraint FKj14lvcomgp7lablhbkx2f7hrp 
       foreign key (booking_id) 
       references booking (booking_id);

    alter table booking_change_log 
       add constraint FK9a54ebbesyn4vthc8k8nkjm22 
       foreign key (new_court_id) 
       references court (court_id);

    alter table booking_change_log 
       add constraint FKgbqo4nhh83vqqd5dshffu19ow 
       foreign key (old_court_id) 
       references court (court_id);

    alter table booking_slot 
       add constraint FKq5d98cx093epox9u3spe2jcxw 
       foreign key (booking_id) 
       references booking (booking_id);

    alter table booking_slot 
       add constraint FK6k10g0gdoelgwmlr9gvm8otgn 
       foreign key (court_id) 
       references court (court_id);

    alter table commission_change_log 
       add constraint FKrxygn6fv1emlcpjoucw164x6n 
       foreign key (changed_by) 
       references account (account_id);

    alter table commission_change_log 
       add constraint FKhgv1p789veqk3jkudvn26jn1x 
       foreign key (new_tier_id) 
       references commission_tier (tier_id);

    alter table commission_change_log 
       add constraint FKn3poqalsmvccdgvrhivwp4dh0 
       foreign key (old_tier_id) 
       references commission_tier (tier_id);

    alter table commission_policy 
       add constraint FKo6ixex39svs4ahh6atgs08ri3 
       foreign key (updated_by) 
       references account (account_id);

    alter table commission_tier 
       add constraint FKqjmk5flqsu2y0qdv80m2ayfhm 
       foreign key (created_by) 
       references account (account_id);

    alter table court 
       add constraint FKnp646tyaa8dlfc6vgigqakww2 
       foreign key (facility_sport_id) 
       references facility_sport (facility_sport_id);

    alter table court_attribute_value 
       add constraint FKo97x0o4wlmdqn57mbavww72nh 
       foreign key (attribute_id) 
       references sport_attribute (attribute_id);

    alter table court_attribute_value 
       add constraint FKqhof5sbc0wnqlotpcqtu0esxm 
       foreign key (court_id) 
       references court (court_id);

    alter table court_schedule_exception 
       add constraint FKhqusfl723oy0nrnrk03yjpkmq 
       foreign key (court_id) 
       references court (court_id);

    alter table court_schedule_exception 
       add constraint FK9qo8kolyo4fa6ux02vxiuawo0 
       foreign key (created_by) 
       references staff (staff_id);

    alter table court_schedule_exception 
       add constraint FKl22or8cxo4a36qs25unwlkhow 
       foreign key (facility_id) 
       references facility (facility_id);

    alter table court_slot_booking 
       add constraint FK24qfggdwxfwy3vx1qcibdxpod 
       foreign key (booking_slot_id) 
       references booking_slot (booking_slot_id);

    alter table court_slot_booking 
       add constraint FKr1ilo3uku5yxc7s2ys89gt3ql 
       foreign key (court_id) 
       references court (court_id);

    alter table customer_favorite_facility 
       add constraint FKfjrnc2rrq1gnh2oc58lg23xle 
       foreign key (account_id) 
       references account (account_id);

    alter table customer_favorite_facility 
       add constraint FKk7fc6jc0vsrns8m5bltar3sya 
       foreign key (facility_id) 
       references facility (facility_id);

    alter table email_queue 
       add constraint FK97k0psx5jo5nsp5wbwq6u27uv 
       foreign key (booking_id) 
       references booking (booking_id);

    alter table facility 
       add constraint FKdn4v673vwnsce108nyf77pdfa 
       foreign key (owner_account_id) 
       references account (account_id);

    alter table facility_image 
       add constraint FKnrx8tu5s53v5tutiyv2sxw01d 
       foreign key (facility_id) 
       references facility (facility_id);

    alter table facility_price_rule 
       add constraint FKkgdipn6ecv56ew5beggx57yyo 
       foreign key (facility_sport_id) 
       references facility_sport (facility_sport_id);

    alter table facility_sport 
       add constraint FKhjl2x22yxxchd7a04o4tphvx9 
       foreign key (facility_id) 
       references facility (facility_id);

    alter table facility_sport 
       add constraint FK1t4y83xcd33kr3pj0f9nju7m6 
       foreign key (sport_id) 
       references sport (sport_id);

    alter table invoice 
       add constraint FK4jd6uuk7w0d72riyre2w14fl7 
       foreign key (booking_id) 
       references booking (booking_id);

    alter table invoice 
       add constraint FKh8mc37lrohbk7stgatwwn5doq 
       foreign key (voucher_id) 
       references voucher (voucher_id);

    alter table notification 
       add constraint FKj0b1ncedmpl7sx7t7o54t26v2 
       foreign key (account_id) 
       references account (account_id);

    alter table order_item 
       add constraint FKselo28ymemephorc1otqe7lcq 
       foreign key (booking_id) 
       references booking (booking_id);

    alter table order_item 
       add constraint FK551losx9j75ss5d6bfsqvijna 
       foreign key (product_id) 
       references product (product_id);

    alter table owner_profile 
       add constraint FKfjr685c3fuyy9iil9jnrd5v5e 
       foreign key (account_id) 
       references account (account_id);

    alter table owner_profile 
       add constraint FKj879nrm2n76uodcldu5a3jdbk 
       foreign key (approved_by) 
       references account (account_id);

    alter table payment 
       add constraint FKsb24p8f52refbb80qwp4gem9n 
       foreign key (invoice_id) 
       references invoice (invoice_id);

    alter table payment 
       add constraint FKa1s5fly67g3hxiaqa4cylyj9m 
       foreign key (staff_confirm_id) 
       references staff (staff_id);

    alter table platform_commission 
       add constraint FKbxouaeg0eaffbye01ij57255f 
       foreign key (commission_tier_id) 
       references commission_tier (tier_id);

    alter table platform_commission 
       add constraint FKlh81n7x1pc5yye31wgihvqmjl 
       foreign key (invoice_id) 
       references invoice (invoice_id);

    alter table platform_commission 
       add constraint FKdf5r0uiasavnupx2408d1be7m 
       foreign key (owner_account_id) 
       references account (account_id);

    alter table product 
       add constraint FK5cypb0k23bovo3rn1a5jqs6j4 
       foreign key (category_id) 
       references product_category (category_id);

    alter table product 
       add constraint FKlm02uo7su1hxcyflt4goj66ft 
       foreign key (facility_id) 
       references facility (facility_id);

    alter table review 
       add constraint FKbopwpfvcg5qsfrjtt9svofxg1 
       foreign key (account_id) 
       references account (account_id);

    alter table review 
       add constraint FKk4xawqohtguy5yx5nnpba6yf3 
       foreign key (booking_id) 
       references booking (booking_id);

    alter table sport_attribute 
       add constraint FKk5e7n7xud4b8r3mpb1txnvair 
       foreign key (sport_id) 
       references sport (sport_id);

    alter table staff 
       add constraint FKs9jl798sgmtrl79dm4svocvaw 
       foreign key (account_id) 
       references account (account_id);

    alter table staff 
       add constraint FKl0j7hmn56i24kta6sihk6yt7 
       foreign key (facility_id) 
       references facility (facility_id);

    alter table voucher 
       add constraint FK725xijc1ux79ti07v7e4imncx 
       foreign key (issuer_account_id) 
       references account (account_id);

    alter table voucher_account 
       add constraint FKhiiiwumq9tm0nch1hbyxn8lih 
       foreign key (account_id) 
       references account (account_id);

    alter table voucher_account 
       add constraint FK9623iyj2s1sa228y4iq9t8yjm 
       foreign key (voucher_id) 
       references voucher (voucher_id);

    alter table voucher_facility 
       add constraint FKtfu138wbm7w4vj2rx8gj9ie1b 
       foreign key (facility_id) 
       references facility (facility_id);

    alter table voucher_facility 
       add constraint FKoces5ulf2787risi4335clo2r 
       foreign key (voucher_id) 
       references voucher (voucher_id);

    alter table voucher_usage 
       add constraint FKg6fotlb4b4wakjghl8hm4mmma 
       foreign key (account_id) 
       references account (account_id);

    alter table voucher_usage 
       add constraint FKeiom45vjfhkw6yq2jwe4qiigo 
       foreign key (booking_id) 
       references booking (booking_id);

    alter table voucher_usage 
       add constraint FKc0wr3a0vbrw2wk4c5wk3ld0o1 
       foreign key (invoice_id) 
       references invoice (invoice_id);

    alter table voucher_usage 
       add constraint FKbrrttecc2rpi70ouyw9rjrv1i 
       foreign key (voucher_id) 
       references voucher (voucher_id);

    create table account (
        account_id integer not null auto_increment,
        is_active bit,
        created_at datetime(6),
        avatar_path varchar(255),
        email varchar(255),
        full_name varchar(255) not null,
        google_id varchar(255),
        password_hash varchar(255),
        phone varchar(255),
        role enum ('ADMIN','CUSTOMER','OWNER','STAFF') not null,
        primary key (account_id)
    ) engine=InnoDB;

    create table blog_comment (
        author_account_id integer not null,
        comment_id integer not null auto_increment,
        is_deleted bit,
        moderated_by_account_id integer,
        post_id integer not null,
        created_at datetime(6),
        moderated_at datetime(6),
        updated_at datetime(6),
        content varchar(1000) not null,
        status enum ('APPROVED','PENDING','REJECTED') not null,
        primary key (comment_id)
    ) engine=InnoDB;

    create table blog_post (
        author_account_id integer not null,
        is_deleted bit,
        post_id integer not null auto_increment,
        created_at datetime(6),
        published_at datetime(6),
        updated_at datetime(6),
        status varchar(20) not null,
        title varchar(200) not null,
        summary varchar(500),
        content LONGTEXT not null,
        primary key (post_id)
    ) engine=InnoDB;

    create table blog_reaction (
        account_id integer not null,
        post_id integer not null,
        reaction_id integer not null auto_increment,
        created_at datetime(6),
        emoji_code enum ('ANGRY','HEART','LAUGH','LIKE','SAD','WOW') not null,
        primary key (reaction_id)
    ) engine=InnoDB;

    create table booking (
        account_id integer,
        booking_id integer not null auto_increment,
        facility_id integer not null,
        guest_id integer,
        staff_id integer,
        checkin_time datetime(6),
        checkout_time datetime(6),
        created_at datetime(6),
        hold_expired_at datetime(6),
        note varchar(500),
        booking_status enum ('CANCELLED','COMPLETED','CONFIRMED','EXPIRED','PENDING'),
        primary key (booking_id)
    ) engine=InnoDB;

    create table booking_change_log (
        actor_staff_id integer,
        booking_id integer not null,
        change_id integer not null auto_increment,
        new_booking_date date,
        new_court_id integer,
        new_end_time time(0),
        new_start_time time(0),
        old_booking_date date,
        old_court_id integer,
        old_end_time time(0),
        old_start_time time(0),
        refund_due decimal(12,2),
        change_time datetime(6),
        change_type varchar(20),
        change_action varchar(30),
        etag_after varchar(64),
        etag_before varchar(64),
        reason varchar(500),
        after_data LONGTEXT,
        before_data LONGTEXT,
        note varchar(255),
        primary key (change_id)
    ) engine=InnoDB;

    create table booking_slot (
        booking_date date not null,
        booking_id integer not null,
        booking_slot_id integer not null auto_increment,
        court_id integer not null,
        end_time time(0) not null,
        price_snapshot decimal(12,2) not null,
        start_time time(0) not null,
        checkin_time datetime(6),
        checkout_time datetime(6),
        slot_status enum ('CANCELLED','CHECKED_IN','CHECKED_OUT','NO_SHOW','PENDING'),
        primary key (booking_slot_id)
    ) engine=InnoDB;

    create table commission_change_log (
        change_log_id integer not null auto_increment,
        changed_by integer not null,
        max_price_per_minute decimal(12,2),
        min_price_per_minute decimal(12,2) not null,
        new_rate decimal(5,4) not null,
        new_tier_id integer not null,
        notice_days integer not null,
        old_rate decimal(5,4),
        old_tier_id integer,
        announced_at datetime(6) not null,
        created_at datetime(6),
        effective_from datetime(6) not null,
        reason varchar(500),
        change_type enum ('CREATE','EXPIRE','UPDATE') not null,
        primary key (change_log_id)
    ) engine=InnoDB;

    create table commission_policy (
        min_notice_days integer not null,
        policy_id integer not null auto_increment,
        updated_by integer,
        updated_at datetime(6),
        description varchar(500),
        primary key (policy_id)
    ) engine=InnoDB;

    create table commission_tier (
        commission_rate decimal(5,4) not null,
        created_by integer,
        is_current bit not null,
        max_price_per_minute decimal(12,2),
        min_price_per_minute decimal(12,2) not null,
        notice_days integer,
        tier_id integer not null auto_increment,
        announced_at datetime(6),
        created_at datetime(6),
        effective_from datetime(6) not null,
        effective_to datetime(6),
        updated_at datetime(6),
        description varchar(500),
        status enum ('ACTIVE','ANNOUNCED','DRAFT','EXPIRED'),
        primary key (tier_id)
    ) engine=InnoDB;

    create table court (
        court_id integer not null auto_increment,
        facility_sport_id integer not null,
        is_active bit,
        description varchar(500),
        court_name varchar(255) not null,
        primary key (court_id)
    ) engine=InnoDB;

    create table court_attribute_value (
        attribute_id integer not null,
        court_id integer not null,
        id integer not null auto_increment,
        value varchar(255) not null,
        primary key (id)
    ) engine=InnoDB;

    create table court_schedule_exception (
        court_id integer not null,
        created_by integer,
        end_date date not null,
        end_time time(0),
        exception_id integer not null auto_increment,
        facility_id integer not null,
        is_active bit,
        start_date date not null,
        start_time time(0),
        created_at datetime(6),
        updated_at datetime(6),
        exception_type varchar(20),
        reason varchar(300),
        primary key (exception_id)
    ) engine=InnoDB;

    create table court_slot_booking (
        booking_date date not null,
        booking_slot_id integer not null,
        court_id integer not null,
        end_time time(0) not null,
        id integer not null auto_increment,
        start_time time(0) not null,
        primary key (id)
    ) engine=InnoDB;

    create table customer_favorite_facility (
        account_id integer not null,
        facility_id integer not null,
        favorite_id integer not null auto_increment,
        primary key (favorite_id)
    ) engine=InnoDB;

    create table email_queue (
        booking_id integer,
        email_id integer not null auto_increment,
        retry_count integer not null,
        created_at datetime(6) not null,
        next_attempt_at datetime(6) not null,
        reminder_at datetime(6),
        sent_at datetime(6),
        status varchar(20) not null,
        email_type varchar(30) not null,
        last_error varchar(500),
        payload_json LONGTEXT,
        to_email varchar(255) not null,
        primary key (email_id)
    ) engine=InnoDB;

    create table email_verification (
        id integer not null auto_increment,
        created_at datetime(6),
        expire_at datetime(6) not null,
        phone varchar(20),
        email varchar(255) not null,
        full_name varchar(255) not null,
        password_hash varchar(255) not null,
        token varchar(255) not null,
        role enum ('ADMIN','CUSTOMER','OWNER','STAFF') not null,
        primary key (id)
    ) engine=InnoDB;

    create table facility (
        close_time time(0) not null,
        facility_id integer not null auto_increment,
        is_active bit,
        latitude decimal(38,2),
        longitude decimal(38,2),
        open_time time(0) not null,
        owner_account_id integer not null,
        created_at datetime(6),
        address varchar(255) not null,
        description LONGTEXT,
        district varchar(255),
        name varchar(255) not null,
        province varchar(255),
        ward varchar(255),
        approval_status enum ('APPROVED','PENDING','REJECTED'),
        primary key (facility_id)
    ) engine=InnoDB;

    create table facility_image (
        facility_id integer not null,
        image_id integer not null auto_increment,
        is_thumbnail bit,
        created_at datetime(6),
        image_path varchar(255) not null,
        primary key (image_id)
    ) engine=InnoDB;

    create table facility_price_rule (
        effective_from date not null,
        effective_to date,
        end_time time(0) not null,
        facility_sport_id integer not null,
        is_active bit,
        price_per_slot decimal(12,2) not null,
        price_rule_id integer not null auto_increment,
        start_time time(0) not null,
        created_at datetime(6),
        day_type enum ('HOLIDAY','WEEKDAY','WEEKEND') not null,
        primary key (price_rule_id)
    ) engine=InnoDB;

    create table facility_sport (
        facility_id integer not null,
        facility_sport_id integer not null auto_increment,
        is_active bit,
        min_duration_minutes integer not null,
        slot_step_minutes integer not null,
        sport_id integer not null,
        primary key (facility_sport_id)
    ) engine=InnoDB;

    create table guest (
        guest_id integer not null auto_increment,
        email varchar(255),
        guest_name varchar(255) not null,
        phone varchar(255) not null,
        primary key (guest_id)
    ) engine=InnoDB;

    create table invoice (
        booking_id integer not null,
        court_amount decimal(12,2) not null,
        deposit_percent integer,
        discount_amount decimal(12,2) not null,
        invoice_id integer not null auto_increment,
        paid_amount decimal(12,2),
        product_amount decimal(12,2) not null,
        refund_due decimal(12,2) not null,
        subtotal decimal(12,2) not null,
        total_amount decimal(12,2) not null,
        voucher_id integer,
        created_at datetime(6),
        refund_note varchar(500),
        payment_status enum ('PAID','PARTIAL','UNPAID'),
        refund_status enum ('NONE','PENDING_MANUAL','REFUNDED'),
        primary key (invoice_id)
    ) engine=InnoDB;

    create table notification (
        account_id integer not null,
        is_read bit,
        is_sent bit,
        notification_id integer not null auto_increment,
        created_at datetime(6),
        type varchar(10),
        content varchar(500),
        title varchar(255),
        primary key (notification_id)
    ) engine=InnoDB;

    create table order_item (
        booking_id integer not null,
        order_item_id integer not null auto_increment,
        product_id integer not null,
        quantity integer not null,
        rental_duration integer,
        total_amount decimal(12,2) not null,
        unit_price_snapshot decimal(12,2) not null,
        created_at datetime(6),
        added_by varchar(10),
        primary key (order_item_id)
    ) engine=InnoDB;

    create table owner_profile (
        account_id integer not null,
        approved_by integer,
        owner_profile_id integer not null auto_increment,
        approved_at datetime(6),
        created_at datetime(6),
        bank_account_name varchar(255),
        bank_account_no varchar(255),
        bank_name varchar(255),
        business_name varchar(255) not null,
        tax_code varchar(255),
        approval_status enum ('APPROVED','PENDING','REJECTED'),
        primary key (owner_profile_id)
    ) engine=InnoDB;

    create table password_reset_token (
        id integer not null auto_increment,
        created_at datetime(6) not null,
        expire_at datetime(6) not null,
        email varchar(255) not null,
        token varchar(255) not null,
        primary key (id)
    ) engine=InnoDB;

    create table payment (
        invoice_id integer not null,
        paid_amount decimal(12,2),
        payment_id integer not null auto_increment,
        staff_confirm_id integer,
        confirm_time datetime(6),
        created_at datetime(6),
        expire_at datetime(6),
        payment_time datetime(6),
        vnpay_response_code varchar(10),
        transaction_code varchar(100),
        vnpay_txn_no varchar(100),
        method enum ('BANK_TRANSFER','CASH','VNPAY'),
        payment_status enum ('FAILED','PENDING','SUCCESS'),
        payment_type enum ('DEPOSIT','FULL','REMAINING'),
        primary key (payment_id)
    ) engine=InnoDB;

    create table platform_commission (
        commission_amount decimal(12,2) not null,
        commission_id integer not null auto_increment,
        commission_rate decimal(5,4) not null,
        commission_tier_id integer,
        court_revenue decimal(12,2) not null,
        invoice_id integer not null,
        owner_account_id integer not null,
        owner_payout decimal(12,2) not null,
        voucher_cost_owner decimal(12,2),
        voucher_cost_platform decimal(12,2),
        created_at datetime(6),
        settled_at datetime(6),
        status enum ('FAILED','PENDING','SETTLED'),
        primary key (commission_id)
    ) engine=InnoDB;

    create table product (
        category_id integer not null,
        facility_id integer not null,
        is_active bit,
        price decimal(12,2) not null,
        product_id integer not null auto_increment,
        stock_quantity integer,
        created_at datetime(6),
        rental_unit varchar(20),
        description varchar(500),
        image_path varchar(255),
        product_name varchar(255) not null,
        product_type enum ('RENTAL','SALE') not null,
        primary key (product_id)
    ) engine=InnoDB;

    create table product_category (
        category_id integer not null auto_increment,
        is_active bit,
        category_code varchar(30) not null,
        category_name varchar(255) not null,
        primary key (category_id)
    ) engine=InnoDB;

    create table review (
        account_id integer not null,
        booking_id integer not null,
        rating integer not null,
        review_id integer not null auto_increment,
        created_at datetime(6),
        comment varchar(500),
        primary key (review_id)
    ) engine=InnoDB;

    create table sport (
        default_min_duration_minutes integer not null,
        default_slot_step_minutes integer not null,
        is_active bit,
        sport_id integer not null auto_increment,
        sport_code varchar(30) not null,
        icon_path varchar(255),
        sport_name varchar(255) not null,
        primary key (sport_id)
    ) engine=InnoDB;

    create table sport_attribute (
        attribute_id integer not null auto_increment,
        is_required bit,
        sport_id integer not null,
        data_type varchar(20) not null,
        attribute_code varchar(50) not null,
        attribute_name varchar(255) not null,
        options_json LONGTEXT,
        primary key (attribute_id)
    ) engine=InnoDB;

    create table staff (
        account_id integer not null,
        facility_id integer not null,
        is_active bit,
        staff_id integer not null auto_increment,
        primary key (staff_id)
    ) engine=InnoDB;

    create table voucher (
        discount_value decimal(12,2) not null,
        is_active bit,
        issuer_account_id integer,
        max_discount_amount decimal(12,2),
        min_order_amount decimal(12,2),
        per_user_limit integer,
        usage_limit integer,
        voucher_id integer not null auto_increment,
        created_at datetime(6),
        valid_from datetime(6) not null,
        valid_to datetime(6) not null,
        code varchar(50) not null,
        description varchar(500),
        name varchar(255) not null,
        applicable_to enum ('ALL','COURT_BOOKING','PRODUCT'),
        discount_type enum ('FIXED_AMOUNT','PERCENTAGE') not null,
        issuer_type enum ('OWNER','PLATFORM') not null,
        primary key (voucher_id)
    ) engine=InnoDB;

    create table voucher_account (
        account_id integer not null,
        voucher_id integer not null
    ) engine=InnoDB;

    create table voucher_facility (
        facility_id integer not null,
        voucher_id integer not null
    ) engine=InnoDB;

    create table voucher_usage (
        account_id integer,
        booking_id integer not null,
        discount_amount decimal(12,2) not null,
        invoice_id integer not null,
        usage_id integer not null auto_increment,
        voucher_id integer not null,
        used_at datetime(6),
        liability_party varchar(10) not null,
        primary key (usage_id)
    ) engine=InnoDB;

    alter table account 
       add constraint UKq0uja26qgu1atulenwup9rxyr unique (email);

    alter table account 
       add constraint UKdgdnj692f2g5ebicy1xyc2l3w unique (phone);

    alter table blog_reaction 
       add constraint UQ_BlogReaction unique (post_id, account_id, emoji_code);

    alter table court_attribute_value 
       add constraint UKj4qa3uj33gp8awnx64rtgrn9r unique (court_id, attribute_id);

    alter table court_slot_booking 
       add constraint UKlwv7d7t524r3bxmi0h523xs9p unique (court_id, booking_date, start_time);

    alter table court_slot_booking 
       add constraint UKceeulgl6uatuliycmothuay41 unique (booking_slot_id);

    alter table customer_favorite_facility 
       add constraint UKq90nef112fe35x5g3ad9042u7 unique (account_id, facility_id);

    alter table facility_sport 
       add constraint UKdxig1mrlph0g5uqke1em81bl4 unique (facility_id, sport_id);

    alter table invoice 
       add constraint UK32ywtxrkeu1wnmivu6mlcqdid unique (booking_id);

    alter table owner_profile 
       add constraint UKhnrcmp4lyxkm7l0p5uegwmw8j unique (account_id);

    alter table password_reset_token 
       add constraint UKg0guo4k8krgpwuagos61oc06j unique (token);

    alter table platform_commission 
       add constraint UKc4yic37b8li7uaejl34er5xkd unique (invoice_id);

    alter table product_category 
       add constraint UKn4nmm8o4cegxl3lt9p48mklvj unique (category_code);

    alter table review 
       add constraint UKm685o801uf70i84jf94qq3d0b unique (booking_id);

    alter table sport 
       add constraint UK91qpqfcq5rjyanobl8fkxsch1 unique (sport_code);

    alter table staff 
       add constraint UK4uqyb8awsv3mfncjj737o7oo9 unique (account_id);

    alter table voucher 
       add constraint UKpvh1lqheshnjoekevvwla03xn unique (code);

    alter table blog_comment 
       add constraint FKtrmc5ogfx7w1u0u5uxo2nl93f 
       foreign key (author_account_id) 
       references account (account_id);

    alter table blog_comment 
       add constraint FKmbe5fcwvss5qwqlj6o4vpiwvl 
       foreign key (moderated_by_account_id) 
       references account (account_id);

    alter table blog_comment 
       add constraint FKeh1bvld0i4iq1rnw951g518l8 
       foreign key (post_id) 
       references blog_post (post_id);

    alter table blog_post 
       add constraint FK1wbk80unrcd3nis0hiwwfuwxu 
       foreign key (author_account_id) 
       references account (account_id);

    alter table blog_reaction 
       add constraint FKdh1rphxlewbgndyi4t8jw9w8j 
       foreign key (account_id) 
       references account (account_id);

    alter table blog_reaction 
       add constraint FKpxoeu71558cb4xvrhoj3g8biq 
       foreign key (post_id) 
       references blog_post (post_id);

    alter table booking 
       add constraint FK7hunottedmjhtdcvhv4sx6x4a 
       foreign key (account_id) 
       references account (account_id);

    alter table booking 
       add constraint FK6io8j4ov8vlpwc9wc37179ca1 
       foreign key (facility_id) 
       references facility (facility_id);

    alter table booking 
       add constraint FKjn3lsroa8t8h7x5sld9b0ru2u 
       foreign key (guest_id) 
       references guest (guest_id);

    alter table booking 
       add constraint FK357w452pgne0tsl2dra6ompmx 
       foreign key (staff_id) 
       references staff (staff_id);

    alter table booking_change_log 
       add constraint FKcnnm4t7shy0wf77mkjlwdgrih 
       foreign key (actor_staff_id) 
       references staff (staff_id);

    alter table booking_change_log 
       add constraint FKj14lvcomgp7lablhbkx2f7hrp 
       foreign key (booking_id) 
       references booking (booking_id);

    alter table booking_change_log 
       add constraint FK9a54ebbesyn4vthc8k8nkjm22 
       foreign key (new_court_id) 
       references court (court_id);

    alter table booking_change_log 
       add constraint FKgbqo4nhh83vqqd5dshffu19ow 
       foreign key (old_court_id) 
       references court (court_id);

    alter table booking_slot 
       add constraint FKq5d98cx093epox9u3spe2jcxw 
       foreign key (booking_id) 
       references booking (booking_id);

    alter table booking_slot 
       add constraint FK6k10g0gdoelgwmlr9gvm8otgn 
       foreign key (court_id) 
       references court (court_id);

    alter table commission_change_log 
       add constraint FKrxygn6fv1emlcpjoucw164x6n 
       foreign key (changed_by) 
       references account (account_id);

    alter table commission_change_log 
       add constraint FKhgv1p789veqk3jkudvn26jn1x 
       foreign key (new_tier_id) 
       references commission_tier (tier_id);

    alter table commission_change_log 
       add constraint FKn3poqalsmvccdgvrhivwp4dh0 
       foreign key (old_tier_id) 
       references commission_tier (tier_id);

    alter table commission_policy 
       add constraint FKo6ixex39svs4ahh6atgs08ri3 
       foreign key (updated_by) 
       references account (account_id);

    alter table commission_tier 
       add constraint FKqjmk5flqsu2y0qdv80m2ayfhm 
       foreign key (created_by) 
       references account (account_id);

    alter table court 
       add constraint FKnp646tyaa8dlfc6vgigqakww2 
       foreign key (facility_sport_id) 
       references facility_sport (facility_sport_id);

    alter table court_attribute_value 
       add constraint FKo97x0o4wlmdqn57mbavww72nh 
       foreign key (attribute_id) 
       references sport_attribute (attribute_id);

    alter table court_attribute_value 
       add constraint FKqhof5sbc0wnqlotpcqtu0esxm 
       foreign key (court_id) 
       references court (court_id);

    alter table court_schedule_exception 
       add constraint FKhqusfl723oy0nrnrk03yjpkmq 
       foreign key (court_id) 
       references court (court_id);

    alter table court_schedule_exception 
       add constraint FK9qo8kolyo4fa6ux02vxiuawo0 
       foreign key (created_by) 
       references staff (staff_id);

    alter table court_schedule_exception 
       add constraint FKl22or8cxo4a36qs25unwlkhow 
       foreign key (facility_id) 
       references facility (facility_id);

    alter table court_slot_booking 
       add constraint FK24qfggdwxfwy3vx1qcibdxpod 
       foreign key (booking_slot_id) 
       references booking_slot (booking_slot_id);

    alter table court_slot_booking 
       add constraint FKr1ilo3uku5yxc7s2ys89gt3ql 
       foreign key (court_id) 
       references court (court_id);

    alter table customer_favorite_facility 
       add constraint FKfjrnc2rrq1gnh2oc58lg23xle 
       foreign key (account_id) 
       references account (account_id);

    alter table customer_favorite_facility 
       add constraint FKk7fc6jc0vsrns8m5bltar3sya 
       foreign key (facility_id) 
       references facility (facility_id);

    alter table email_queue 
       add constraint FK97k0psx5jo5nsp5wbwq6u27uv 
       foreign key (booking_id) 
       references booking (booking_id);

    alter table facility 
       add constraint FKdn4v673vwnsce108nyf77pdfa 
       foreign key (owner_account_id) 
       references account (account_id);

    alter table facility_image 
       add constraint FKnrx8tu5s53v5tutiyv2sxw01d 
       foreign key (facility_id) 
       references facility (facility_id);

    alter table facility_price_rule 
       add constraint FKkgdipn6ecv56ew5beggx57yyo 
       foreign key (facility_sport_id) 
       references facility_sport (facility_sport_id);

    alter table facility_sport 
       add constraint FKhjl2x22yxxchd7a04o4tphvx9 
       foreign key (facility_id) 
       references facility (facility_id);

    alter table facility_sport 
       add constraint FK1t4y83xcd33kr3pj0f9nju7m6 
       foreign key (sport_id) 
       references sport (sport_id);

    alter table invoice 
       add constraint FK4jd6uuk7w0d72riyre2w14fl7 
       foreign key (booking_id) 
       references booking (booking_id);

    alter table invoice 
       add constraint FKh8mc37lrohbk7stgatwwn5doq 
       foreign key (voucher_id) 
       references voucher (voucher_id);

    alter table notification 
       add constraint FKj0b1ncedmpl7sx7t7o54t26v2 
       foreign key (account_id) 
       references account (account_id);

    alter table order_item 
       add constraint FKselo28ymemephorc1otqe7lcq 
       foreign key (booking_id) 
       references booking (booking_id);

    alter table order_item 
       add constraint FK551losx9j75ss5d6bfsqvijna 
       foreign key (product_id) 
       references product (product_id);

    alter table owner_profile 
       add constraint FKfjr685c3fuyy9iil9jnrd5v5e 
       foreign key (account_id) 
       references account (account_id);

    alter table owner_profile 
       add constraint FKj879nrm2n76uodcldu5a3jdbk 
       foreign key (approved_by) 
       references account (account_id);

    alter table payment 
       add constraint FKsb24p8f52refbb80qwp4gem9n 
       foreign key (invoice_id) 
       references invoice (invoice_id);

    alter table payment 
       add constraint FKa1s5fly67g3hxiaqa4cylyj9m 
       foreign key (staff_confirm_id) 
       references staff (staff_id);

    alter table platform_commission 
       add constraint FKbxouaeg0eaffbye01ij57255f 
       foreign key (commission_tier_id) 
       references commission_tier (tier_id);

    alter table platform_commission 
       add constraint FKlh81n7x1pc5yye31wgihvqmjl 
       foreign key (invoice_id) 
       references invoice (invoice_id);

    alter table platform_commission 
       add constraint FKdf5r0uiasavnupx2408d1be7m 
       foreign key (owner_account_id) 
       references account (account_id);

    alter table product 
       add constraint FK5cypb0k23bovo3rn1a5jqs6j4 
       foreign key (category_id) 
       references product_category (category_id);

    alter table product 
       add constraint FKlm02uo7su1hxcyflt4goj66ft 
       foreign key (facility_id) 
       references facility (facility_id);

    alter table review 
       add constraint FKbopwpfvcg5qsfrjtt9svofxg1 
       foreign key (account_id) 
       references account (account_id);

    alter table review 
       add constraint FKk4xawqohtguy5yx5nnpba6yf3 
       foreign key (booking_id) 
       references booking (booking_id);

    alter table sport_attribute 
       add constraint FKk5e7n7xud4b8r3mpb1txnvair 
       foreign key (sport_id) 
       references sport (sport_id);

    alter table staff 
       add constraint FKs9jl798sgmtrl79dm4svocvaw 
       foreign key (account_id) 
       references account (account_id);

    alter table staff 
       add constraint FKl0j7hmn56i24kta6sihk6yt7 
       foreign key (facility_id) 
       references facility (facility_id);

    alter table voucher 
       add constraint FK725xijc1ux79ti07v7e4imncx 
       foreign key (issuer_account_id) 
       references account (account_id);

    alter table voucher_account 
       add constraint FKhiiiwumq9tm0nch1hbyxn8lih 
       foreign key (account_id) 
       references account (account_id);

    alter table voucher_account 
       add constraint FK9623iyj2s1sa228y4iq9t8yjm 
       foreign key (voucher_id) 
       references voucher (voucher_id);

    alter table voucher_facility 
       add constraint FKtfu138wbm7w4vj2rx8gj9ie1b 
       foreign key (facility_id) 
       references facility (facility_id);

    alter table voucher_facility 
       add constraint FKoces5ulf2787risi4335clo2r 
       foreign key (voucher_id) 
       references voucher (voucher_id);

    alter table voucher_usage 
       add constraint FKg6fotlb4b4wakjghl8hm4mmma 
       foreign key (account_id) 
       references account (account_id);

    alter table voucher_usage 
       add constraint FKeiom45vjfhkw6yq2jwe4qiigo 
       foreign key (booking_id) 
       references booking (booking_id);

    alter table voucher_usage 
       add constraint FKc0wr3a0vbrw2wk4c5wk3ld0o1 
       foreign key (invoice_id) 
       references invoice (invoice_id);

    alter table voucher_usage 
       add constraint FKbrrttecc2rpi70ouyw9rjrv1i 
       foreign key (voucher_id) 
       references voucher (voucher_id);

    create table account (
        account_id integer not null auto_increment,
        is_active bit,
        created_at datetime(6),
        avatar_path varchar(255),
        email varchar(255),
        full_name varchar(255) not null,
        google_id varchar(255),
        password_hash varchar(255),
        phone varchar(255),
        role enum ('ADMIN','CUSTOMER','OWNER','STAFF') not null,
        primary key (account_id)
    ) engine=InnoDB;

    create table blog_comment (
        author_account_id integer not null,
        comment_id integer not null auto_increment,
        is_deleted bit,
        moderated_by_account_id integer,
        post_id integer not null,
        created_at datetime(6),
        moderated_at datetime(6),
        updated_at datetime(6),
        content varchar(1000) not null,
        status enum ('APPROVED','PENDING','REJECTED') not null,
        primary key (comment_id)
    ) engine=InnoDB;

    create table blog_post (
        author_account_id integer not null,
        is_deleted bit,
        post_id integer not null auto_increment,
        created_at datetime(6),
        published_at datetime(6),
        updated_at datetime(6),
        status varchar(20) not null,
        title varchar(200) not null,
        summary varchar(500),
        content LONGTEXT not null,
        primary key (post_id)
    ) engine=InnoDB;

    create table blog_reaction (
        account_id integer not null,
        post_id integer not null,
        reaction_id integer not null auto_increment,
        created_at datetime(6),
        emoji_code enum ('ANGRY','HEART','LAUGH','LIKE','SAD','WOW') not null,
        primary key (reaction_id)
    ) engine=InnoDB;

    create table booking (
        account_id integer,
        booking_id integer not null auto_increment,
        facility_id integer not null,
        guest_id integer,
        staff_id integer,
        checkin_time datetime(6),
        checkout_time datetime(6),
        created_at datetime(6),
        hold_expired_at datetime(6),
        note varchar(500),
        booking_status enum ('CANCELLED','COMPLETED','CONFIRMED','EXPIRED','PENDING'),
        primary key (booking_id)
    ) engine=InnoDB;

    create table booking_change_log (
        actor_staff_id integer,
        booking_id integer not null,
        change_id integer not null auto_increment,
        new_booking_date date,
        new_court_id integer,
        new_end_time time(0),
        new_start_time time(0),
        old_booking_date date,
        old_court_id integer,
        old_end_time time(0),
        old_start_time time(0),
        refund_due decimal(12,2),
        change_time datetime(6),
        change_type varchar(20),
        change_action varchar(30),
        etag_after varchar(64),
        etag_before varchar(64),
        reason varchar(500),
        after_data LONGTEXT,
        before_data LONGTEXT,
        note varchar(255),
        primary key (change_id)
    ) engine=InnoDB;

    create table booking_slot (
        booking_date date not null,
        booking_id integer not null,
        booking_slot_id integer not null auto_increment,
        court_id integer not null,
        end_time time(0) not null,
        price_snapshot decimal(12,2) not null,
        start_time time(0) not null,
        checkin_time datetime(6),
        checkout_time datetime(6),
        slot_status enum ('CANCELLED','CHECKED_IN','CHECKED_OUT','NO_SHOW','PENDING'),
        primary key (booking_slot_id)
    ) engine=InnoDB;

    create table commission_change_log (
        change_log_id integer not null auto_increment,
        changed_by integer not null,
        max_price_per_minute decimal(12,2),
        min_price_per_minute decimal(12,2) not null,
        new_rate decimal(5,4) not null,
        new_tier_id integer not null,
        notice_days integer not null,
        old_rate decimal(5,4),
        old_tier_id integer,
        announced_at datetime(6) not null,
        created_at datetime(6),
        effective_from datetime(6) not null,
        reason varchar(500),
        change_type enum ('CREATE','EXPIRE','UPDATE') not null,
        primary key (change_log_id)
    ) engine=InnoDB;

    create table commission_policy (
        min_notice_days integer not null,
        policy_id integer not null auto_increment,
        updated_by integer,
        updated_at datetime(6),
        description varchar(500),
        primary key (policy_id)
    ) engine=InnoDB;

    create table commission_tier (
        commission_rate decimal(5,4) not null,
        created_by integer,
        is_current bit not null,
        max_price_per_minute decimal(12,2),
        min_price_per_minute decimal(12,2) not null,
        notice_days integer,
        tier_id integer not null auto_increment,
        announced_at datetime(6),
        created_at datetime(6),
        effective_from datetime(6) not null,
        effective_to datetime(6),
        updated_at datetime(6),
        description varchar(500),
        status enum ('ACTIVE','ANNOUNCED','DRAFT','EXPIRED'),
        primary key (tier_id)
    ) engine=InnoDB;

    create table court (
        court_id integer not null auto_increment,
        facility_sport_id integer not null,
        is_active bit,
        description varchar(500),
        court_name varchar(255) not null,
        primary key (court_id)
    ) engine=InnoDB;

    create table court_attribute_value (
        attribute_id integer not null,
        court_id integer not null,
        id integer not null auto_increment,
        value varchar(255) not null,
        primary key (id)
    ) engine=InnoDB;

    create table court_schedule_exception (
        court_id integer not null,
        created_by integer,
        end_date date not null,
        end_time time(0),
        exception_id integer not null auto_increment,
        facility_id integer not null,
        is_active bit,
        start_date date not null,
        start_time time(0),
        created_at datetime(6),
        updated_at datetime(6),
        exception_type varchar(20),
        reason varchar(300),
        primary key (exception_id)
    ) engine=InnoDB;

    create table court_slot_booking (
        booking_date date not null,
        booking_slot_id integer not null,
        court_id integer not null,
        end_time time(0) not null,
        id integer not null auto_increment,
        start_time time(0) not null,
        primary key (id)
    ) engine=InnoDB;

    create table customer_favorite_facility (
        account_id integer not null,
        facility_id integer not null,
        favorite_id integer not null auto_increment,
        primary key (favorite_id)
    ) engine=InnoDB;

    create table email_queue (
        booking_id integer,
        email_id integer not null auto_increment,
        retry_count integer not null,
        created_at datetime(6) not null,
        next_attempt_at datetime(6) not null,
        reminder_at datetime(6),
        sent_at datetime(6),
        status varchar(20) not null,
        email_type varchar(30) not null,
        last_error varchar(500),
        payload_json LONGTEXT,
        to_email varchar(255) not null,
        primary key (email_id)
    ) engine=InnoDB;

    create table email_verification (
        id integer not null auto_increment,
        created_at datetime(6),
        expire_at datetime(6) not null,
        phone varchar(20),
        email varchar(255) not null,
        full_name varchar(255) not null,
        password_hash varchar(255) not null,
        token varchar(255) not null,
        role enum ('ADMIN','CUSTOMER','OWNER','STAFF') not null,
        primary key (id)
    ) engine=InnoDB;

    create table facility (
        close_time time(0) not null,
        facility_id integer not null auto_increment,
        is_active bit,
        latitude decimal(38,2),
        longitude decimal(38,2),
        open_time time(0) not null,
        owner_account_id integer not null,
        created_at datetime(6),
        address varchar(255) not null,
        description LONGTEXT,
        district varchar(255),
        name varchar(255) not null,
        province varchar(255),
        ward varchar(255),
        approval_status enum ('APPROVED','PENDING','REJECTED'),
        primary key (facility_id)
    ) engine=InnoDB;

    create table facility_image (
        facility_id integer not null,
        image_id integer not null auto_increment,
        is_thumbnail bit,
        created_at datetime(6),
        image_path varchar(255) not null,
        primary key (image_id)
    ) engine=InnoDB;

    create table facility_price_rule (
        effective_from date not null,
        effective_to date,
        end_time time(0) not null,
        facility_sport_id integer not null,
        is_active bit,
        price_per_slot decimal(12,2) not null,
        price_rule_id integer not null auto_increment,
        start_time time(0) not null,
        created_at datetime(6),
        day_type enum ('HOLIDAY','WEEKDAY','WEEKEND') not null,
        primary key (price_rule_id)
    ) engine=InnoDB;

    create table facility_sport (
        facility_id integer not null,
        facility_sport_id integer not null auto_increment,
        is_active bit,
        min_duration_minutes integer not null,
        slot_step_minutes integer not null,
        sport_id integer not null,
        primary key (facility_sport_id)
    ) engine=InnoDB;

    create table guest (
        guest_id integer not null auto_increment,
        email varchar(255),
        guest_name varchar(255) not null,
        phone varchar(255) not null,
        primary key (guest_id)
    ) engine=InnoDB;

    create table invoice (
        booking_id integer not null,
        court_amount decimal(12,2) not null,
        deposit_percent integer,
        discount_amount decimal(12,2) not null,
        invoice_id integer not null auto_increment,
        paid_amount decimal(12,2),
        product_amount decimal(12,2) not null,
        refund_due decimal(12,2) not null,
        subtotal decimal(12,2) not null,
        total_amount decimal(12,2) not null,
        voucher_id integer,
        created_at datetime(6),
        refund_note varchar(500),
        payment_status enum ('PAID','PARTIAL','UNPAID'),
        refund_status enum ('NONE','PENDING_MANUAL','REFUNDED'),
        primary key (invoice_id)
    ) engine=InnoDB;

    create table notification (
        account_id integer not null,
        is_read bit,
        is_sent bit,
        notification_id integer not null auto_increment,
        created_at datetime(6),
        type varchar(10),
        content varchar(500),
        title varchar(255),
        primary key (notification_id)
    ) engine=InnoDB;

    create table order_item (
        booking_id integer not null,
        order_item_id integer not null auto_increment,
        product_id integer not null,
        quantity integer not null,
        rental_duration integer,
        total_amount decimal(12,2) not null,
        unit_price_snapshot decimal(12,2) not null,
        created_at datetime(6),
        added_by varchar(10),
        primary key (order_item_id)
    ) engine=InnoDB;

    create table owner_profile (
        account_id integer not null,
        approved_by integer,
        owner_profile_id integer not null auto_increment,
        approved_at datetime(6),
        created_at datetime(6),
        bank_account_name varchar(255),
        bank_account_no varchar(255),
        bank_name varchar(255),
        business_name varchar(255) not null,
        tax_code varchar(255),
        approval_status enum ('APPROVED','PENDING','REJECTED'),
        primary key (owner_profile_id)
    ) engine=InnoDB;

    create table password_reset_token (
        id integer not null auto_increment,
        created_at datetime(6) not null,
        expire_at datetime(6) not null,
        email varchar(255) not null,
        token varchar(255) not null,
        primary key (id)
    ) engine=InnoDB;

    create table payment (
        invoice_id integer not null,
        paid_amount decimal(12,2),
        payment_id integer not null auto_increment,
        staff_confirm_id integer,
        confirm_time datetime(6),
        created_at datetime(6),
        expire_at datetime(6),
        payment_time datetime(6),
        vnpay_response_code varchar(10),
        transaction_code varchar(100),
        vnpay_txn_no varchar(100),
        method enum ('BANK_TRANSFER','CASH','VNPAY'),
        payment_status enum ('FAILED','PENDING','SUCCESS'),
        payment_type enum ('DEPOSIT','FULL','REMAINING'),
        primary key (payment_id)
    ) engine=InnoDB;

    create table platform_commission (
        commission_amount decimal(12,2) not null,
        commission_id integer not null auto_increment,
        commission_rate decimal(5,4) not null,
        commission_tier_id integer,
        court_revenue decimal(12,2) not null,
        invoice_id integer not null,
        owner_account_id integer not null,
        owner_payout decimal(12,2) not null,
        voucher_cost_owner decimal(12,2),
        voucher_cost_platform decimal(12,2),
        created_at datetime(6),
        settled_at datetime(6),
        status enum ('FAILED','PENDING','SETTLED'),
        primary key (commission_id)
    ) engine=InnoDB;

    create table product (
        category_id integer not null,
        facility_id integer not null,
        is_active bit,
        price decimal(12,2) not null,
        product_id integer not null auto_increment,
        stock_quantity integer,
        created_at datetime(6),
        rental_unit varchar(20),
        description varchar(500),
        image_path varchar(255),
        product_name varchar(255) not null,
        product_type enum ('RENTAL','SALE') not null,
        primary key (product_id)
    ) engine=InnoDB;

    create table product_category (
        category_id integer not null auto_increment,
        is_active bit,
        category_code varchar(30) not null,
        category_name varchar(255) not null,
        primary key (category_id)
    ) engine=InnoDB;

    create table review (
        account_id integer not null,
        booking_id integer not null,
        rating integer not null,
        review_id integer not null auto_increment,
        created_at datetime(6),
        comment varchar(500),
        primary key (review_id)
    ) engine=InnoDB;

    create table sport (
        default_min_duration_minutes integer not null,
        default_slot_step_minutes integer not null,
        is_active bit,
        sport_id integer not null auto_increment,
        sport_code varchar(30) not null,
        icon_path varchar(255),
        sport_name varchar(255) not null,
        primary key (sport_id)
    ) engine=InnoDB;

    create table sport_attribute (
        attribute_id integer not null auto_increment,
        is_required bit,
        sport_id integer not null,
        data_type varchar(20) not null,
        attribute_code varchar(50) not null,
        attribute_name varchar(255) not null,
        options_json LONGTEXT,
        primary key (attribute_id)
    ) engine=InnoDB;

    create table staff (
        account_id integer not null,
        facility_id integer not null,
        is_active bit,
        staff_id integer not null auto_increment,
        primary key (staff_id)
    ) engine=InnoDB;

    create table voucher (
        discount_value decimal(12,2) not null,
        is_active bit,
        issuer_account_id integer,
        max_discount_amount decimal(12,2),
        min_order_amount decimal(12,2),
        per_user_limit integer,
        usage_limit integer,
        voucher_id integer not null auto_increment,
        created_at datetime(6),
        valid_from datetime(6) not null,
        valid_to datetime(6) not null,
        code varchar(50) not null,
        description varchar(500),
        name varchar(255) not null,
        applicable_to enum ('ALL','COURT_BOOKING','PRODUCT'),
        discount_type enum ('FIXED_AMOUNT','PERCENTAGE') not null,
        issuer_type enum ('OWNER','PLATFORM') not null,
        primary key (voucher_id)
    ) engine=InnoDB;

    create table voucher_account (
        account_id integer not null,
        voucher_id integer not null
    ) engine=InnoDB;

    create table voucher_facility (
        facility_id integer not null,
        voucher_id integer not null
    ) engine=InnoDB;

    create table voucher_usage (
        account_id integer,
        booking_id integer not null,
        discount_amount decimal(12,2) not null,
        invoice_id integer not null,
        usage_id integer not null auto_increment,
        voucher_id integer not null,
        used_at datetime(6),
        liability_party varchar(10) not null,
        primary key (usage_id)
    ) engine=InnoDB;

    alter table account 
       add constraint UKq0uja26qgu1atulenwup9rxyr unique (email);

    alter table account 
       add constraint UKdgdnj692f2g5ebicy1xyc2l3w unique (phone);

    alter table blog_reaction 
       add constraint UQ_BlogReaction unique (post_id, account_id, emoji_code);

    alter table court_attribute_value 
       add constraint UKj4qa3uj33gp8awnx64rtgrn9r unique (court_id, attribute_id);

    alter table court_slot_booking 
       add constraint UKlwv7d7t524r3bxmi0h523xs9p unique (court_id, booking_date, start_time);

    alter table court_slot_booking 
       add constraint UKceeulgl6uatuliycmothuay41 unique (booking_slot_id);

    alter table customer_favorite_facility 
       add constraint UKq90nef112fe35x5g3ad9042u7 unique (account_id, facility_id);

    alter table facility_sport 
       add constraint UKdxig1mrlph0g5uqke1em81bl4 unique (facility_id, sport_id);

    alter table invoice 
       add constraint UK32ywtxrkeu1wnmivu6mlcqdid unique (booking_id);

    alter table owner_profile 
       add constraint UKhnrcmp4lyxkm7l0p5uegwmw8j unique (account_id);

    alter table password_reset_token 
       add constraint UKg0guo4k8krgpwuagos61oc06j unique (token);

    alter table platform_commission 
       add constraint UKc4yic37b8li7uaejl34er5xkd unique (invoice_id);

    alter table product_category 
       add constraint UKn4nmm8o4cegxl3lt9p48mklvj unique (category_code);

    alter table review 
       add constraint UKm685o801uf70i84jf94qq3d0b unique (booking_id);

    alter table sport 
       add constraint UK91qpqfcq5rjyanobl8fkxsch1 unique (sport_code);

    alter table staff 
       add constraint UK4uqyb8awsv3mfncjj737o7oo9 unique (account_id);

    alter table voucher 
       add constraint UKpvh1lqheshnjoekevvwla03xn unique (code);

    alter table blog_comment 
       add constraint FKtrmc5ogfx7w1u0u5uxo2nl93f 
       foreign key (author_account_id) 
       references account (account_id);

    alter table blog_comment 
       add constraint FKmbe5fcwvss5qwqlj6o4vpiwvl 
       foreign key (moderated_by_account_id) 
       references account (account_id);

    alter table blog_comment 
       add constraint FKeh1bvld0i4iq1rnw951g518l8 
       foreign key (post_id) 
       references blog_post (post_id);

    alter table blog_post 
       add constraint FK1wbk80unrcd3nis0hiwwfuwxu 
       foreign key (author_account_id) 
       references account (account_id);

    alter table blog_reaction 
       add constraint FKdh1rphxlewbgndyi4t8jw9w8j 
       foreign key (account_id) 
       references account (account_id);

    alter table blog_reaction 
       add constraint FKpxoeu71558cb4xvrhoj3g8biq 
       foreign key (post_id) 
       references blog_post (post_id);

    alter table booking 
       add constraint FK7hunottedmjhtdcvhv4sx6x4a 
       foreign key (account_id) 
       references account (account_id);

    alter table booking 
       add constraint FK6io8j4ov8vlpwc9wc37179ca1 
       foreign key (facility_id) 
       references facility (facility_id);

    alter table booking 
       add constraint FKjn3lsroa8t8h7x5sld9b0ru2u 
       foreign key (guest_id) 
       references guest (guest_id);

    alter table booking 
       add constraint FK357w452pgne0tsl2dra6ompmx 
       foreign key (staff_id) 
       references staff (staff_id);

    alter table booking_change_log 
       add constraint FKcnnm4t7shy0wf77mkjlwdgrih 
       foreign key (actor_staff_id) 
       references staff (staff_id);

    alter table booking_change_log 
       add constraint FKj14lvcomgp7lablhbkx2f7hrp 
       foreign key (booking_id) 
       references booking (booking_id);

    alter table booking_change_log 
       add constraint FK9a54ebbesyn4vthc8k8nkjm22 
       foreign key (new_court_id) 
       references court (court_id);

    alter table booking_change_log 
       add constraint FKgbqo4nhh83vqqd5dshffu19ow 
       foreign key (old_court_id) 
       references court (court_id);

    alter table booking_slot 
       add constraint FKq5d98cx093epox9u3spe2jcxw 
       foreign key (booking_id) 
       references booking (booking_id);

    alter table booking_slot 
       add constraint FK6k10g0gdoelgwmlr9gvm8otgn 
       foreign key (court_id) 
       references court (court_id);

    alter table commission_change_log 
       add constraint FKrxygn6fv1emlcpjoucw164x6n 
       foreign key (changed_by) 
       references account (account_id);

    alter table commission_change_log 
       add constraint FKhgv1p789veqk3jkudvn26jn1x 
       foreign key (new_tier_id) 
       references commission_tier (tier_id);

    alter table commission_change_log 
       add constraint FKn3poqalsmvccdgvrhivwp4dh0 
       foreign key (old_tier_id) 
       references commission_tier (tier_id);

    alter table commission_policy 
       add constraint FKo6ixex39svs4ahh6atgs08ri3 
       foreign key (updated_by) 
       references account (account_id);

    alter table commission_tier 
       add constraint FKqjmk5flqsu2y0qdv80m2ayfhm 
       foreign key (created_by) 
       references account (account_id);

    alter table court 
       add constraint FKnp646tyaa8dlfc6vgigqakww2 
       foreign key (facility_sport_id) 
       references facility_sport (facility_sport_id);

    alter table court_attribute_value 
       add constraint FKo97x0o4wlmdqn57mbavww72nh 
       foreign key (attribute_id) 
       references sport_attribute (attribute_id);

    alter table court_attribute_value 
       add constraint FKqhof5sbc0wnqlotpcqtu0esxm 
       foreign key (court_id) 
       references court (court_id);

    alter table court_schedule_exception 
       add constraint FKhqusfl723oy0nrnrk03yjpkmq 
       foreign key (court_id) 
       references court (court_id);

    alter table court_schedule_exception 
       add constraint FK9qo8kolyo4fa6ux02vxiuawo0 
       foreign key (created_by) 
       references staff (staff_id);

    alter table court_schedule_exception 
       add constraint FKl22or8cxo4a36qs25unwlkhow 
       foreign key (facility_id) 
       references facility (facility_id);

    alter table court_slot_booking 
       add constraint FK24qfggdwxfwy3vx1qcibdxpod 
       foreign key (booking_slot_id) 
       references booking_slot (booking_slot_id);

    alter table court_slot_booking 
       add constraint FKr1ilo3uku5yxc7s2ys89gt3ql 
       foreign key (court_id) 
       references court (court_id);

    alter table customer_favorite_facility 
       add constraint FKfjrnc2rrq1gnh2oc58lg23xle 
       foreign key (account_id) 
       references account (account_id);

    alter table customer_favorite_facility 
       add constraint FKk7fc6jc0vsrns8m5bltar3sya 
       foreign key (facility_id) 
       references facility (facility_id);

    alter table email_queue 
       add constraint FK97k0psx5jo5nsp5wbwq6u27uv 
       foreign key (booking_id) 
       references booking (booking_id);

    alter table facility 
       add constraint FKdn4v673vwnsce108nyf77pdfa 
       foreign key (owner_account_id) 
       references account (account_id);

    alter table facility_image 
       add constraint FKnrx8tu5s53v5tutiyv2sxw01d 
       foreign key (facility_id) 
       references facility (facility_id);

    alter table facility_price_rule 
       add constraint FKkgdipn6ecv56ew5beggx57yyo 
       foreign key (facility_sport_id) 
       references facility_sport (facility_sport_id);

    alter table facility_sport 
       add constraint FKhjl2x22yxxchd7a04o4tphvx9 
       foreign key (facility_id) 
       references facility (facility_id);

    alter table facility_sport 
       add constraint FK1t4y83xcd33kr3pj0f9nju7m6 
       foreign key (sport_id) 
       references sport (sport_id);

    alter table invoice 
       add constraint FK4jd6uuk7w0d72riyre2w14fl7 
       foreign key (booking_id) 
       references booking (booking_id);

    alter table invoice 
       add constraint FKh8mc37lrohbk7stgatwwn5doq 
       foreign key (voucher_id) 
       references voucher (voucher_id);

    alter table notification 
       add constraint FKj0b1ncedmpl7sx7t7o54t26v2 
       foreign key (account_id) 
       references account (account_id);

    alter table order_item 
       add constraint FKselo28ymemephorc1otqe7lcq 
       foreign key (booking_id) 
       references booking (booking_id);

    alter table order_item 
       add constraint FK551losx9j75ss5d6bfsqvijna 
       foreign key (product_id) 
       references product (product_id);

    alter table owner_profile 
       add constraint FKfjr685c3fuyy9iil9jnrd5v5e 
       foreign key (account_id) 
       references account (account_id);

    alter table owner_profile 
       add constraint FKj879nrm2n76uodcldu5a3jdbk 
       foreign key (approved_by) 
       references account (account_id);

    alter table payment 
       add constraint FKsb24p8f52refbb80qwp4gem9n 
       foreign key (invoice_id) 
       references invoice (invoice_id);

    alter table payment 
       add constraint FKa1s5fly67g3hxiaqa4cylyj9m 
       foreign key (staff_confirm_id) 
       references staff (staff_id);

    alter table platform_commission 
       add constraint FKbxouaeg0eaffbye01ij57255f 
       foreign key (commission_tier_id) 
       references commission_tier (tier_id);

    alter table platform_commission 
       add constraint FKlh81n7x1pc5yye31wgihvqmjl 
       foreign key (invoice_id) 
       references invoice (invoice_id);

    alter table platform_commission 
       add constraint FKdf5r0uiasavnupx2408d1be7m 
       foreign key (owner_account_id) 
       references account (account_id);

    alter table product 
       add constraint FK5cypb0k23bovo3rn1a5jqs6j4 
       foreign key (category_id) 
       references product_category (category_id);

    alter table product 
       add constraint FKlm02uo7su1hxcyflt4goj66ft 
       foreign key (facility_id) 
       references facility (facility_id);

    alter table review 
       add constraint FKbopwpfvcg5qsfrjtt9svofxg1 
       foreign key (account_id) 
       references account (account_id);

    alter table review 
       add constraint FKk4xawqohtguy5yx5nnpba6yf3 
       foreign key (booking_id) 
       references booking (booking_id);

    alter table sport_attribute 
       add constraint FKk5e7n7xud4b8r3mpb1txnvair 
       foreign key (sport_id) 
       references sport (sport_id);

    alter table staff 
       add constraint FKs9jl798sgmtrl79dm4svocvaw 
       foreign key (account_id) 
       references account (account_id);

    alter table staff 
       add constraint FKl0j7hmn56i24kta6sihk6yt7 
       foreign key (facility_id) 
       references facility (facility_id);

    alter table voucher 
       add constraint FK725xijc1ux79ti07v7e4imncx 
       foreign key (issuer_account_id) 
       references account (account_id);

    alter table voucher_account 
       add constraint FKhiiiwumq9tm0nch1hbyxn8lih 
       foreign key (account_id) 
       references account (account_id);

    alter table voucher_account 
       add constraint FK9623iyj2s1sa228y4iq9t8yjm 
       foreign key (voucher_id) 
       references voucher (voucher_id);

    alter table voucher_facility 
       add constraint FKtfu138wbm7w4vj2rx8gj9ie1b 
       foreign key (facility_id) 
       references facility (facility_id);

    alter table voucher_facility 
       add constraint FKoces5ulf2787risi4335clo2r 
       foreign key (voucher_id) 
       references voucher (voucher_id);

    alter table voucher_usage 
       add constraint FKg6fotlb4b4wakjghl8hm4mmma 
       foreign key (account_id) 
       references account (account_id);

    alter table voucher_usage 
       add constraint FKeiom45vjfhkw6yq2jwe4qiigo 
       foreign key (booking_id) 
       references booking (booking_id);

    alter table voucher_usage 
       add constraint FKc0wr3a0vbrw2wk4c5wk3ld0o1 
       foreign key (invoice_id) 
       references invoice (invoice_id);

    alter table voucher_usage 
       add constraint FKbrrttecc2rpi70ouyw9rjrv1i 
       foreign key (voucher_id) 
       references voucher (voucher_id);

    create table account (
        account_id integer not null auto_increment,
        is_active bit,
        created_at datetime(6),
        avatar_path varchar(255),
        email varchar(255),
        full_name varchar(255) not null,
        google_id varchar(255),
        password_hash varchar(255),
        phone varchar(255),
        role enum ('ADMIN','CUSTOMER','OWNER','STAFF') not null,
        primary key (account_id)
    ) engine=InnoDB;

    create table blog_comment (
        author_account_id integer not null,
        comment_id integer not null auto_increment,
        is_deleted bit,
        moderated_by_account_id integer,
        post_id integer not null,
        created_at datetime(6),
        moderated_at datetime(6),
        updated_at datetime(6),
        content varchar(1000) not null,
        status enum ('APPROVED','PENDING','REJECTED') not null,
        primary key (comment_id)
    ) engine=InnoDB;

    create table blog_post (
        author_account_id integer not null,
        is_deleted bit,
        post_id integer not null auto_increment,
        created_at datetime(6),
        published_at datetime(6),
        updated_at datetime(6),
        status varchar(20) not null,
        title varchar(200) not null,
        summary varchar(500),
        content LONGTEXT not null,
        primary key (post_id)
    ) engine=InnoDB;

    create table blog_reaction (
        account_id integer not null,
        post_id integer not null,
        reaction_id integer not null auto_increment,
        created_at datetime(6),
        emoji_code enum ('ANGRY','HEART','LAUGH','LIKE','SAD','WOW') not null,
        primary key (reaction_id)
    ) engine=InnoDB;

    create table booking (
        account_id integer,
        booking_id integer not null auto_increment,
        facility_id integer not null,
        guest_id integer,
        staff_id integer,
        checkin_time datetime(6),
        checkout_time datetime(6),
        created_at datetime(6),
        hold_expired_at datetime(6),
        note varchar(500),
        booking_status enum ('CANCELLED','COMPLETED','CONFIRMED','EXPIRED','PENDING'),
        primary key (booking_id)
    ) engine=InnoDB;

    create table booking_change_log (
        actor_staff_id integer,
        booking_id integer not null,
        change_id integer not null auto_increment,
        new_booking_date date,
        new_court_id integer,
        new_end_time time(0),
        new_start_time time(0),
        old_booking_date date,
        old_court_id integer,
        old_end_time time(0),
        old_start_time time(0),
        refund_due decimal(12,2),
        change_time datetime(6),
        change_type varchar(20),
        change_action varchar(30),
        etag_after varchar(64),
        etag_before varchar(64),
        reason varchar(500),
        after_data LONGTEXT,
        before_data LONGTEXT,
        note varchar(255),
        primary key (change_id)
    ) engine=InnoDB;

    create table booking_slot (
        booking_date date not null,
        booking_id integer not null,
        booking_slot_id integer not null auto_increment,
        court_id integer not null,
        end_time time(0) not null,
        price_snapshot decimal(12,2) not null,
        start_time time(0) not null,
        checkin_time datetime(6),
        checkout_time datetime(6),
        slot_status enum ('CANCELLED','CHECKED_IN','CHECKED_OUT','NO_SHOW','PENDING'),
        primary key (booking_slot_id)
    ) engine=InnoDB;

    create table commission_change_log (
        change_log_id integer not null auto_increment,
        changed_by integer not null,
        max_price_per_minute decimal(12,2),
        min_price_per_minute decimal(12,2) not null,
        new_rate decimal(5,4) not null,
        new_tier_id integer not null,
        notice_days integer not null,
        old_rate decimal(5,4),
        old_tier_id integer,
        announced_at datetime(6) not null,
        created_at datetime(6),
        effective_from datetime(6) not null,
        reason varchar(500),
        change_type enum ('CREATE','EXPIRE','UPDATE') not null,
        primary key (change_log_id)
    ) engine=InnoDB;

    create table commission_policy (
        min_notice_days integer not null,
        policy_id integer not null auto_increment,
        updated_by integer,
        updated_at datetime(6),
        description varchar(500),
        primary key (policy_id)
    ) engine=InnoDB;

    create table commission_tier (
        commission_rate decimal(5,4) not null,
        created_by integer,
        is_current bit not null,
        max_price_per_minute decimal(12,2),
        min_price_per_minute decimal(12,2) not null,
        notice_days integer,
        tier_id integer not null auto_increment,
        announced_at datetime(6),
        created_at datetime(6),
        effective_from datetime(6) not null,
        effective_to datetime(6),
        updated_at datetime(6),
        description varchar(500),
        status enum ('ACTIVE','ANNOUNCED','DRAFT','EXPIRED'),
        primary key (tier_id)
    ) engine=InnoDB;

    create table court (
        court_id integer not null auto_increment,
        facility_sport_id integer not null,
        is_active bit,
        description varchar(500),
        court_name varchar(255) not null,
        primary key (court_id)
    ) engine=InnoDB;

    create table court_attribute_value (
        attribute_id integer not null,
        court_id integer not null,
        id integer not null auto_increment,
        value varchar(255) not null,
        primary key (id)
    ) engine=InnoDB;

    create table court_schedule_exception (
        court_id integer not null,
        created_by integer,
        end_date date not null,
        end_time time(0),
        exception_id integer not null auto_increment,
        facility_id integer not null,
        is_active bit,
        start_date date not null,
        start_time time(0),
        created_at datetime(6),
        updated_at datetime(6),
        exception_type varchar(20),
        reason varchar(300),
        primary key (exception_id)
    ) engine=InnoDB;

    create table court_slot_booking (
        booking_date date not null,
        booking_slot_id integer not null,
        court_id integer not null,
        end_time time(0) not null,
        id integer not null auto_increment,
        start_time time(0) not null,
        primary key (id)
    ) engine=InnoDB;

    create table customer_favorite_facility (
        account_id integer not null,
        facility_id integer not null,
        favorite_id integer not null auto_increment,
        primary key (favorite_id)
    ) engine=InnoDB;

    create table email_queue (
        booking_id integer,
        email_id integer not null auto_increment,
        retry_count integer not null,
        created_at datetime(6) not null,
        next_attempt_at datetime(6) not null,
        reminder_at datetime(6),
        sent_at datetime(6),
        status varchar(20) not null,
        email_type varchar(30) not null,
        last_error varchar(500),
        payload_json LONGTEXT,
        to_email varchar(255) not null,
        primary key (email_id)
    ) engine=InnoDB;

    create table email_verification (
        id integer not null auto_increment,
        created_at datetime(6),
        expire_at datetime(6) not null,
        phone varchar(20),
        email varchar(255) not null,
        full_name varchar(255) not null,
        password_hash varchar(255) not null,
        token varchar(255) not null,
        role enum ('ADMIN','CUSTOMER','OWNER','STAFF') not null,
        primary key (id)
    ) engine=InnoDB;

    create table facility (
        close_time time(0) not null,
        facility_id integer not null auto_increment,
        is_active bit,
        latitude decimal(38,2),
        longitude decimal(38,2),
        open_time time(0) not null,
        owner_account_id integer not null,
        created_at datetime(6),
        address varchar(255) not null,
        description LONGTEXT,
        district varchar(255),
        name varchar(255) not null,
        province varchar(255),
        ward varchar(255),
        approval_status enum ('APPROVED','PENDING','REJECTED'),
        primary key (facility_id)
    ) engine=InnoDB;

    create table facility_image (
        facility_id integer not null,
        image_id integer not null auto_increment,
        is_thumbnail bit,
        created_at datetime(6),
        image_path varchar(255) not null,
        primary key (image_id)
    ) engine=InnoDB;

    create table facility_price_rule (
        effective_from date not null,
        effective_to date,
        end_time time(0) not null,
        facility_sport_id integer not null,
        is_active bit,
        price_per_slot decimal(12,2) not null,
        price_rule_id integer not null auto_increment,
        start_time time(0) not null,
        created_at datetime(6),
        day_type enum ('HOLIDAY','WEEKDAY','WEEKEND') not null,
        primary key (price_rule_id)
    ) engine=InnoDB;

    create table facility_sport (
        facility_id integer not null,
        facility_sport_id integer not null auto_increment,
        is_active bit,
        min_duration_minutes integer not null,
        slot_step_minutes integer not null,
        sport_id integer not null,
        primary key (facility_sport_id)
    ) engine=InnoDB;

    create table guest (
        guest_id integer not null auto_increment,
        email varchar(255),
        guest_name varchar(255) not null,
        phone varchar(255) not null,
        primary key (guest_id)
    ) engine=InnoDB;

    create table invoice (
        booking_id integer not null,
        court_amount decimal(12,2) not null,
        deposit_percent integer,
        discount_amount decimal(12,2) not null,
        invoice_id integer not null auto_increment,
        paid_amount decimal(12,2),
        product_amount decimal(12,2) not null,
        refund_due decimal(12,2) not null,
        subtotal decimal(12,2) not null,
        total_amount decimal(12,2) not null,
        voucher_id integer,
        created_at datetime(6),
        refund_note varchar(500),
        payment_status enum ('PAID','PARTIAL','UNPAID'),
        refund_status enum ('NONE','PENDING_MANUAL','REFUNDED'),
        primary key (invoice_id)
    ) engine=InnoDB;

    create table notification (
        account_id integer not null,
        is_read bit,
        is_sent bit,
        notification_id integer not null auto_increment,
        created_at datetime(6),
        type varchar(10),
        content varchar(500),
        title varchar(255),
        primary key (notification_id)
    ) engine=InnoDB;

    create table order_item (
        booking_id integer not null,
        order_item_id integer not null auto_increment,
        product_id integer not null,
        quantity integer not null,
        rental_duration integer,
        total_amount decimal(12,2) not null,
        unit_price_snapshot decimal(12,2) not null,
        created_at datetime(6),
        added_by varchar(10),
        primary key (order_item_id)
    ) engine=InnoDB;

    create table owner_profile (
        account_id integer not null,
        approved_by integer,
        owner_profile_id integer not null auto_increment,
        approved_at datetime(6),
        created_at datetime(6),
        bank_account_name varchar(255),
        bank_account_no varchar(255),
        bank_name varchar(255),
        business_name varchar(255) not null,
        tax_code varchar(255),
        approval_status enum ('APPROVED','PENDING','REJECTED'),
        primary key (owner_profile_id)
    ) engine=InnoDB;

    create table password_reset_token (
        id integer not null auto_increment,
        created_at datetime(6) not null,
        expire_at datetime(6) not null,
        email varchar(255) not null,
        token varchar(255) not null,
        primary key (id)
    ) engine=InnoDB;

    create table payment (
        invoice_id integer not null,
        paid_amount decimal(12,2),
        payment_id integer not null auto_increment,
        staff_confirm_id integer,
        confirm_time datetime(6),
        created_at datetime(6),
        expire_at datetime(6),
        payment_time datetime(6),
        vnpay_response_code varchar(10),
        transaction_code varchar(100),
        vnpay_txn_no varchar(100),
        method enum ('BANK_TRANSFER','CASH','VNPAY'),
        payment_status enum ('FAILED','PENDING','SUCCESS'),
        payment_type enum ('DEPOSIT','FULL','REMAINING'),
        primary key (payment_id)
    ) engine=InnoDB;

    create table platform_commission (
        commission_amount decimal(12,2) not null,
        commission_id integer not null auto_increment,
        commission_rate decimal(5,4) not null,
        commission_tier_id integer,
        court_revenue decimal(12,2) not null,
        invoice_id integer not null,
        owner_account_id integer not null,
        owner_payout decimal(12,2) not null,
        voucher_cost_owner decimal(12,2),
        voucher_cost_platform decimal(12,2),
        created_at datetime(6),
        settled_at datetime(6),
        status enum ('FAILED','PENDING','SETTLED'),
        primary key (commission_id)
    ) engine=InnoDB;

    create table product (
        category_id integer not null,
        facility_id integer not null,
        is_active bit,
        price decimal(12,2) not null,
        product_id integer not null auto_increment,
        stock_quantity integer,
        created_at datetime(6),
        rental_unit varchar(20),
        description varchar(500),
        image_path varchar(255),
        product_name varchar(255) not null,
        product_type enum ('RENTAL','SALE') not null,
        primary key (product_id)
    ) engine=InnoDB;

    create table product_category (
        category_id integer not null auto_increment,
        is_active bit,
        category_code varchar(30) not null,
        category_name varchar(255) not null,
        primary key (category_id)
    ) engine=InnoDB;

    create table review (
        account_id integer not null,
        booking_id integer not null,
        rating integer not null,
        review_id integer not null auto_increment,
        created_at datetime(6),
        comment varchar(500),
        primary key (review_id)
    ) engine=InnoDB;

    create table sport (
        default_min_duration_minutes integer not null,
        default_slot_step_minutes integer not null,
        is_active bit,
        sport_id integer not null auto_increment,
        sport_code varchar(30) not null,
        icon_path varchar(255),
        sport_name varchar(255) not null,
        primary key (sport_id)
    ) engine=InnoDB;

    create table sport_attribute (
        attribute_id integer not null auto_increment,
        is_required bit,
        sport_id integer not null,
        data_type varchar(20) not null,
        attribute_code varchar(50) not null,
        attribute_name varchar(255) not null,
        options_json LONGTEXT,
        primary key (attribute_id)
    ) engine=InnoDB;

    create table staff (
        account_id integer not null,
        facility_id integer not null,
        is_active bit,
        staff_id integer not null auto_increment,
        primary key (staff_id)
    ) engine=InnoDB;

    create table voucher (
        discount_value decimal(12,2) not null,
        is_active bit,
        issuer_account_id integer,
        max_discount_amount decimal(12,2),
        min_order_amount decimal(12,2),
        per_user_limit integer,
        usage_limit integer,
        voucher_id integer not null auto_increment,
        created_at datetime(6),
        valid_from datetime(6) not null,
        valid_to datetime(6) not null,
        code varchar(50) not null,
        description varchar(500),
        name varchar(255) not null,
        applicable_to enum ('ALL','COURT_BOOKING','PRODUCT'),
        discount_type enum ('FIXED_AMOUNT','PERCENTAGE') not null,
        issuer_type enum ('OWNER','PLATFORM') not null,
        primary key (voucher_id)
    ) engine=InnoDB;

    create table voucher_account (
        account_id integer not null,
        voucher_id integer not null
    ) engine=InnoDB;

    create table voucher_facility (
        facility_id integer not null,
        voucher_id integer not null
    ) engine=InnoDB;

    create table voucher_usage (
        account_id integer,
        booking_id integer not null,
        discount_amount decimal(12,2) not null,
        invoice_id integer not null,
        usage_id integer not null auto_increment,
        voucher_id integer not null,
        used_at datetime(6),
        liability_party varchar(10) not null,
        primary key (usage_id)
    ) engine=InnoDB;

    alter table account 
       add constraint UKq0uja26qgu1atulenwup9rxyr unique (email);

    alter table account 
       add constraint UKdgdnj692f2g5ebicy1xyc2l3w unique (phone);

    alter table blog_reaction 
       add constraint UQ_BlogReaction unique (post_id, account_id, emoji_code);

    alter table court_attribute_value 
       add constraint UKj4qa3uj33gp8awnx64rtgrn9r unique (court_id, attribute_id);

    alter table court_slot_booking 
       add constraint UKlwv7d7t524r3bxmi0h523xs9p unique (court_id, booking_date, start_time);

    alter table court_slot_booking 
       add constraint UKceeulgl6uatuliycmothuay41 unique (booking_slot_id);

    alter table customer_favorite_facility 
       add constraint UKq90nef112fe35x5g3ad9042u7 unique (account_id, facility_id);

    alter table facility_sport 
       add constraint UKdxig1mrlph0g5uqke1em81bl4 unique (facility_id, sport_id);

    alter table invoice 
       add constraint UK32ywtxrkeu1wnmivu6mlcqdid unique (booking_id);

    alter table owner_profile 
       add constraint UKhnrcmp4lyxkm7l0p5uegwmw8j unique (account_id);

    alter table password_reset_token 
       add constraint UKg0guo4k8krgpwuagos61oc06j unique (token);

    alter table platform_commission 
       add constraint UKc4yic37b8li7uaejl34er5xkd unique (invoice_id);

    alter table product_category 
       add constraint UKn4nmm8o4cegxl3lt9p48mklvj unique (category_code);

    alter table review 
       add constraint UKm685o801uf70i84jf94qq3d0b unique (booking_id);

    alter table sport 
       add constraint UK91qpqfcq5rjyanobl8fkxsch1 unique (sport_code);

    alter table staff 
       add constraint UK4uqyb8awsv3mfncjj737o7oo9 unique (account_id);

    alter table voucher 
       add constraint UKpvh1lqheshnjoekevvwla03xn unique (code);

    alter table blog_comment 
       add constraint FKtrmc5ogfx7w1u0u5uxo2nl93f 
       foreign key (author_account_id) 
       references account (account_id);

    alter table blog_comment 
       add constraint FKmbe5fcwvss5qwqlj6o4vpiwvl 
       foreign key (moderated_by_account_id) 
       references account (account_id);

    alter table blog_comment 
       add constraint FKeh1bvld0i4iq1rnw951g518l8 
       foreign key (post_id) 
       references blog_post (post_id);

    alter table blog_post 
       add constraint FK1wbk80unrcd3nis0hiwwfuwxu 
       foreign key (author_account_id) 
       references account (account_id);

    alter table blog_reaction 
       add constraint FKdh1rphxlewbgndyi4t8jw9w8j 
       foreign key (account_id) 
       references account (account_id);

    alter table blog_reaction 
       add constraint FKpxoeu71558cb4xvrhoj3g8biq 
       foreign key (post_id) 
       references blog_post (post_id);

    alter table booking 
       add constraint FK7hunottedmjhtdcvhv4sx6x4a 
       foreign key (account_id) 
       references account (account_id);

    alter table booking 
       add constraint FK6io8j4ov8vlpwc9wc37179ca1 
       foreign key (facility_id) 
       references facility (facility_id);

    alter table booking 
       add constraint FKjn3lsroa8t8h7x5sld9b0ru2u 
       foreign key (guest_id) 
       references guest (guest_id);

    alter table booking 
       add constraint FK357w452pgne0tsl2dra6ompmx 
       foreign key (staff_id) 
       references staff (staff_id);

    alter table booking_change_log 
       add constraint FKcnnm4t7shy0wf77mkjlwdgrih 
       foreign key (actor_staff_id) 
       references staff (staff_id);

    alter table booking_change_log 
       add constraint FKj14lvcomgp7lablhbkx2f7hrp 
       foreign key (booking_id) 
       references booking (booking_id);

    alter table booking_change_log 
       add constraint FK9a54ebbesyn4vthc8k8nkjm22 
       foreign key (new_court_id) 
       references court (court_id);

    alter table booking_change_log 
       add constraint FKgbqo4nhh83vqqd5dshffu19ow 
       foreign key (old_court_id) 
       references court (court_id);

    alter table booking_slot 
       add constraint FKq5d98cx093epox9u3spe2jcxw 
       foreign key (booking_id) 
       references booking (booking_id);

    alter table booking_slot 
       add constraint FK6k10g0gdoelgwmlr9gvm8otgn 
       foreign key (court_id) 
       references court (court_id);

    alter table commission_change_log 
       add constraint FKrxygn6fv1emlcpjoucw164x6n 
       foreign key (changed_by) 
       references account (account_id);

    alter table commission_change_log 
       add constraint FKhgv1p789veqk3jkudvn26jn1x 
       foreign key (new_tier_id) 
       references commission_tier (tier_id);

    alter table commission_change_log 
       add constraint FKn3poqalsmvccdgvrhivwp4dh0 
       foreign key (old_tier_id) 
       references commission_tier (tier_id);

    alter table commission_policy 
       add constraint FKo6ixex39svs4ahh6atgs08ri3 
       foreign key (updated_by) 
       references account (account_id);

    alter table commission_tier 
       add constraint FKqjmk5flqsu2y0qdv80m2ayfhm 
       foreign key (created_by) 
       references account (account_id);

    alter table court 
       add constraint FKnp646tyaa8dlfc6vgigqakww2 
       foreign key (facility_sport_id) 
       references facility_sport (facility_sport_id);

    alter table court_attribute_value 
       add constraint FKo97x0o4wlmdqn57mbavww72nh 
       foreign key (attribute_id) 
       references sport_attribute (attribute_id);

    alter table court_attribute_value 
       add constraint FKqhof5sbc0wnqlotpcqtu0esxm 
       foreign key (court_id) 
       references court (court_id);

    alter table court_schedule_exception 
       add constraint FKhqusfl723oy0nrnrk03yjpkmq 
       foreign key (court_id) 
       references court (court_id);

    alter table court_schedule_exception 
       add constraint FK9qo8kolyo4fa6ux02vxiuawo0 
       foreign key (created_by) 
       references staff (staff_id);

    alter table court_schedule_exception 
       add constraint FKl22or8cxo4a36qs25unwlkhow 
       foreign key (facility_id) 
       references facility (facility_id);

    alter table court_slot_booking 
       add constraint FK24qfggdwxfwy3vx1qcibdxpod 
       foreign key (booking_slot_id) 
       references booking_slot (booking_slot_id);

    alter table court_slot_booking 
       add constraint FKr1ilo3uku5yxc7s2ys89gt3ql 
       foreign key (court_id) 
       references court (court_id);

    alter table customer_favorite_facility 
       add constraint FKfjrnc2rrq1gnh2oc58lg23xle 
       foreign key (account_id) 
       references account (account_id);

    alter table customer_favorite_facility 
       add constraint FKk7fc6jc0vsrns8m5bltar3sya 
       foreign key (facility_id) 
       references facility (facility_id);

    alter table email_queue 
       add constraint FK97k0psx5jo5nsp5wbwq6u27uv 
       foreign key (booking_id) 
       references booking (booking_id);

    alter table facility 
       add constraint FKdn4v673vwnsce108nyf77pdfa 
       foreign key (owner_account_id) 
       references account (account_id);

    alter table facility_image 
       add constraint FKnrx8tu5s53v5tutiyv2sxw01d 
       foreign key (facility_id) 
       references facility (facility_id);

    alter table facility_price_rule 
       add constraint FKkgdipn6ecv56ew5beggx57yyo 
       foreign key (facility_sport_id) 
       references facility_sport (facility_sport_id);

    alter table facility_sport 
       add constraint FKhjl2x22yxxchd7a04o4tphvx9 
       foreign key (facility_id) 
       references facility (facility_id);

    alter table facility_sport 
       add constraint FK1t4y83xcd33kr3pj0f9nju7m6 
       foreign key (sport_id) 
       references sport (sport_id);

    alter table invoice 
       add constraint FK4jd6uuk7w0d72riyre2w14fl7 
       foreign key (booking_id) 
       references booking (booking_id);

    alter table invoice 
       add constraint FKh8mc37lrohbk7stgatwwn5doq 
       foreign key (voucher_id) 
       references voucher (voucher_id);

    alter table notification 
       add constraint FKj0b1ncedmpl7sx7t7o54t26v2 
       foreign key (account_id) 
       references account (account_id);

    alter table order_item 
       add constraint FKselo28ymemephorc1otqe7lcq 
       foreign key (booking_id) 
       references booking (booking_id);

    alter table order_item 
       add constraint FK551losx9j75ss5d6bfsqvijna 
       foreign key (product_id) 
       references product (product_id);

    alter table owner_profile 
       add constraint FKfjr685c3fuyy9iil9jnrd5v5e 
       foreign key (account_id) 
       references account (account_id);

    alter table owner_profile 
       add constraint FKj879nrm2n76uodcldu5a3jdbk 
       foreign key (approved_by) 
       references account (account_id);

    alter table payment 
       add constraint FKsb24p8f52refbb80qwp4gem9n 
       foreign key (invoice_id) 
       references invoice (invoice_id);

    alter table payment 
       add constraint FKa1s5fly67g3hxiaqa4cylyj9m 
       foreign key (staff_confirm_id) 
       references staff (staff_id);

    alter table platform_commission 
       add constraint FKbxouaeg0eaffbye01ij57255f 
       foreign key (commission_tier_id) 
       references commission_tier (tier_id);

    alter table platform_commission 
       add constraint FKlh81n7x1pc5yye31wgihvqmjl 
       foreign key (invoice_id) 
       references invoice (invoice_id);

    alter table platform_commission 
       add constraint FKdf5r0uiasavnupx2408d1be7m 
       foreign key (owner_account_id) 
       references account (account_id);

    alter table product 
       add constraint FK5cypb0k23bovo3rn1a5jqs6j4 
       foreign key (category_id) 
       references product_category (category_id);

    alter table product 
       add constraint FKlm02uo7su1hxcyflt4goj66ft 
       foreign key (facility_id) 
       references facility (facility_id);

    alter table review 
       add constraint FKbopwpfvcg5qsfrjtt9svofxg1 
       foreign key (account_id) 
       references account (account_id);

    alter table review 
       add constraint FKk4xawqohtguy5yx5nnpba6yf3 
       foreign key (booking_id) 
       references booking (booking_id);

    alter table sport_attribute 
       add constraint FKk5e7n7xud4b8r3mpb1txnvair 
       foreign key (sport_id) 
       references sport (sport_id);

    alter table staff 
       add constraint FKs9jl798sgmtrl79dm4svocvaw 
       foreign key (account_id) 
       references account (account_id);

    alter table staff 
       add constraint FKl0j7hmn56i24kta6sihk6yt7 
       foreign key (facility_id) 
       references facility (facility_id);

    alter table voucher 
       add constraint FK725xijc1ux79ti07v7e4imncx 
       foreign key (issuer_account_id) 
       references account (account_id);

    alter table voucher_account 
       add constraint FKhiiiwumq9tm0nch1hbyxn8lih 
       foreign key (account_id) 
       references account (account_id);

    alter table voucher_account 
       add constraint FK9623iyj2s1sa228y4iq9t8yjm 
       foreign key (voucher_id) 
       references voucher (voucher_id);

    alter table voucher_facility 
       add constraint FKtfu138wbm7w4vj2rx8gj9ie1b 
       foreign key (facility_id) 
       references facility (facility_id);

    alter table voucher_facility 
       add constraint FKoces5ulf2787risi4335clo2r 
       foreign key (voucher_id) 
       references voucher (voucher_id);

    alter table voucher_usage 
       add constraint FKg6fotlb4b4wakjghl8hm4mmma 
       foreign key (account_id) 
       references account (account_id);

    alter table voucher_usage 
       add constraint FKeiom45vjfhkw6yq2jwe4qiigo 
       foreign key (booking_id) 
       references booking (booking_id);

    alter table voucher_usage 
       add constraint FKc0wr3a0vbrw2wk4c5wk3ld0o1 
       foreign key (invoice_id) 
       references invoice (invoice_id);

    alter table voucher_usage 
       add constraint FKbrrttecc2rpi70ouyw9rjrv1i 
       foreign key (voucher_id) 
       references voucher (voucher_id);

    create table account (
        account_id integer not null auto_increment,
        is_active bit,
        created_at datetime(6),
        avatar_path varchar(255),
        email varchar(255),
        full_name varchar(255) not null,
        google_id varchar(255),
        password_hash varchar(255),
        phone varchar(255),
        role enum ('ADMIN','CUSTOMER','OWNER','STAFF') not null,
        primary key (account_id)
    ) engine=InnoDB;

    create table blog_comment (
        author_account_id integer not null,
        comment_id integer not null auto_increment,
        is_deleted bit,
        moderated_by_account_id integer,
        post_id integer not null,
        created_at datetime(6),
        moderated_at datetime(6),
        updated_at datetime(6),
        content varchar(1000) not null,
        status enum ('APPROVED','PENDING','REJECTED') not null,
        primary key (comment_id)
    ) engine=InnoDB;

    create table blog_post (
        author_account_id integer not null,
        is_deleted bit,
        post_id integer not null auto_increment,
        created_at datetime(6),
        published_at datetime(6),
        updated_at datetime(6),
        status varchar(20) not null,
        title varchar(200) not null,
        summary varchar(500),
        content LONGTEXT not null,
        primary key (post_id)
    ) engine=InnoDB;

    create table blog_reaction (
        account_id integer not null,
        post_id integer not null,
        reaction_id integer not null auto_increment,
        created_at datetime(6),
        emoji_code enum ('ANGRY','HEART','LAUGH','LIKE','SAD','WOW') not null,
        primary key (reaction_id)
    ) engine=InnoDB;

    create table booking (
        account_id integer,
        booking_id integer not null auto_increment,
        facility_id integer not null,
        guest_id integer,
        staff_id integer,
        checkin_time datetime(6),
        checkout_time datetime(6),
        created_at datetime(6),
        hold_expired_at datetime(6),
        note varchar(500),
        booking_status enum ('CANCELLED','COMPLETED','CONFIRMED','EXPIRED','PENDING'),
        primary key (booking_id)
    ) engine=InnoDB;

    create table booking_change_log (
        actor_staff_id integer,
        booking_id integer not null,
        change_id integer not null auto_increment,
        new_booking_date date,
        new_court_id integer,
        new_end_time time(0),
        new_start_time time(0),
        old_booking_date date,
        old_court_id integer,
        old_end_time time(0),
        old_start_time time(0),
        refund_due decimal(12,2),
        change_time datetime(6),
        change_type varchar(20),
        change_action varchar(30),
        etag_after varchar(64),
        etag_before varchar(64),
        reason varchar(500),
        after_data LONGTEXT,
        before_data LONGTEXT,
        note varchar(255),
        primary key (change_id)
    ) engine=InnoDB;

    create table booking_slot (
        booking_date date not null,
        booking_id integer not null,
        booking_slot_id integer not null auto_increment,
        court_id integer not null,
        end_time time(0) not null,
        price_snapshot decimal(12,2) not null,
        start_time time(0) not null,
        checkin_time datetime(6),
        checkout_time datetime(6),
        slot_status enum ('CANCELLED','CHECKED_IN','CHECKED_OUT','NO_SHOW','PENDING'),
        primary key (booking_slot_id)
    ) engine=InnoDB;

    create table commission_change_log (
        change_log_id integer not null auto_increment,
        changed_by integer not null,
        max_price_per_minute decimal(12,2),
        min_price_per_minute decimal(12,2) not null,
        new_rate decimal(5,4) not null,
        new_tier_id integer not null,
        notice_days integer not null,
        old_rate decimal(5,4),
        old_tier_id integer,
        announced_at datetime(6) not null,
        created_at datetime(6),
        effective_from datetime(6) not null,
        reason varchar(500),
        change_type enum ('CREATE','EXPIRE','UPDATE') not null,
        primary key (change_log_id)
    ) engine=InnoDB;

    create table commission_policy (
        min_notice_days integer not null,
        policy_id integer not null auto_increment,
        updated_by integer,
        updated_at datetime(6),
        description varchar(500),
        primary key (policy_id)
    ) engine=InnoDB;

    create table commission_tier (
        commission_rate decimal(5,4) not null,
        created_by integer,
        is_current bit not null,
        max_price_per_minute decimal(12,2),
        min_price_per_minute decimal(12,2) not null,
        notice_days integer,
        tier_id integer not null auto_increment,
        announced_at datetime(6),
        created_at datetime(6),
        effective_from datetime(6) not null,
        effective_to datetime(6),
        updated_at datetime(6),
        description varchar(500),
        status enum ('ACTIVE','ANNOUNCED','DRAFT','EXPIRED'),
        primary key (tier_id)
    ) engine=InnoDB;

    create table court (
        court_id integer not null auto_increment,
        facility_sport_id integer not null,
        is_active bit,
        description varchar(500),
        court_name varchar(255) not null,
        primary key (court_id)
    ) engine=InnoDB;

    create table court_attribute_value (
        attribute_id integer not null,
        court_id integer not null,
        id integer not null auto_increment,
        value varchar(255) not null,
        primary key (id)
    ) engine=InnoDB;

    create table court_schedule_exception (
        court_id integer not null,
        created_by integer,
        end_date date not null,
        end_time time(0),
        exception_id integer not null auto_increment,
        facility_id integer not null,
        is_active bit,
        start_date date not null,
        start_time time(0),
        created_at datetime(6),
        updated_at datetime(6),
        exception_type varchar(20),
        reason varchar(300),
        primary key (exception_id)
    ) engine=InnoDB;

    create table court_slot_booking (
        booking_date date not null,
        booking_slot_id integer not null,
        court_id integer not null,
        end_time time(0) not null,
        id integer not null auto_increment,
        start_time time(0) not null,
        primary key (id)
    ) engine=InnoDB;

    create table customer_favorite_facility (
        account_id integer not null,
        facility_id integer not null,
        favorite_id integer not null auto_increment,
        primary key (favorite_id)
    ) engine=InnoDB;

    create table email_queue (
        booking_id integer,
        email_id integer not null auto_increment,
        retry_count integer not null,
        created_at datetime(6) not null,
        next_attempt_at datetime(6) not null,
        reminder_at datetime(6),
        sent_at datetime(6),
        status varchar(20) not null,
        email_type varchar(30) not null,
        last_error varchar(500),
        payload_json LONGTEXT,
        to_email varchar(255) not null,
        primary key (email_id)
    ) engine=InnoDB;

    create table email_verification (
        id integer not null auto_increment,
        created_at datetime(6),
        expire_at datetime(6) not null,
        phone varchar(20),
        email varchar(255) not null,
        full_name varchar(255) not null,
        password_hash varchar(255) not null,
        token varchar(255) not null,
        role enum ('ADMIN','CUSTOMER','OWNER','STAFF') not null,
        primary key (id)
    ) engine=InnoDB;

    create table facility (
        close_time time(0) not null,
        facility_id integer not null auto_increment,
        is_active bit,
        latitude decimal(38,2),
        longitude decimal(38,2),
        open_time time(0) not null,
        owner_account_id integer not null,
        created_at datetime(6),
        address varchar(255) not null,
        description LONGTEXT,
        district varchar(255),
        name varchar(255) not null,
        province varchar(255),
        ward varchar(255),
        approval_status enum ('APPROVED','PENDING','REJECTED'),
        primary key (facility_id)
    ) engine=InnoDB;

    create table facility_image (
        facility_id integer not null,
        image_id integer not null auto_increment,
        is_thumbnail bit,
        created_at datetime(6),
        image_path varchar(255) not null,
        primary key (image_id)
    ) engine=InnoDB;

    create table facility_price_rule (
        effective_from date not null,
        effective_to date,
        end_time time(0) not null,
        facility_sport_id integer not null,
        is_active bit,
        price_per_slot decimal(12,2) not null,
        price_rule_id integer not null auto_increment,
        start_time time(0) not null,
        created_at datetime(6),
        day_type enum ('HOLIDAY','WEEKDAY','WEEKEND') not null,
        primary key (price_rule_id)
    ) engine=InnoDB;

    create table facility_sport (
        facility_id integer not null,
        facility_sport_id integer not null auto_increment,
        is_active bit,
        min_duration_minutes integer not null,
        slot_step_minutes integer not null,
        sport_id integer not null,
        primary key (facility_sport_id)
    ) engine=InnoDB;

    create table guest (
        guest_id integer not null auto_increment,
        email varchar(255),
        guest_name varchar(255) not null,
        phone varchar(255) not null,
        primary key (guest_id)
    ) engine=InnoDB;

    create table invoice (
        booking_id integer not null,
        court_amount decimal(12,2) not null,
        deposit_percent integer,
        discount_amount decimal(12,2) not null,
        invoice_id integer not null auto_increment,
        paid_amount decimal(12,2),
        product_amount decimal(12,2) not null,
        refund_due decimal(12,2) not null,
        subtotal decimal(12,2) not null,
        total_amount decimal(12,2) not null,
        voucher_id integer,
        created_at datetime(6),
        refund_note varchar(500),
        payment_status enum ('PAID','PARTIAL','UNPAID'),
        refund_status enum ('NONE','PENDING_MANUAL','REFUNDED'),
        primary key (invoice_id)
    ) engine=InnoDB;

    create table notification (
        account_id integer not null,
        is_read bit,
        is_sent bit,
        notification_id integer not null auto_increment,
        created_at datetime(6),
        type varchar(10),
        content varchar(500),
        title varchar(255),
        primary key (notification_id)
    ) engine=InnoDB;

    create table order_item (
        booking_id integer not null,
        order_item_id integer not null auto_increment,
        product_id integer not null,
        quantity integer not null,
        rental_duration integer,
        total_amount decimal(12,2) not null,
        unit_price_snapshot decimal(12,2) not null,
        created_at datetime(6),
        added_by varchar(10),
        primary key (order_item_id)
    ) engine=InnoDB;

    create table owner_profile (
        account_id integer not null,
        approved_by integer,
        owner_profile_id integer not null auto_increment,
        approved_at datetime(6),
        created_at datetime(6),
        bank_account_name varchar(255),
        bank_account_no varchar(255),
        bank_name varchar(255),
        business_name varchar(255) not null,
        tax_code varchar(255),
        approval_status enum ('APPROVED','PENDING','REJECTED'),
        primary key (owner_profile_id)
    ) engine=InnoDB;

    create table password_reset_token (
        id integer not null auto_increment,
        created_at datetime(6) not null,
        expire_at datetime(6) not null,
        email varchar(255) not null,
        token varchar(255) not null,
        primary key (id)
    ) engine=InnoDB;

    create table payment (
        invoice_id integer not null,
        paid_amount decimal(12,2),
        payment_id integer not null auto_increment,
        staff_confirm_id integer,
        confirm_time datetime(6),
        created_at datetime(6),
        expire_at datetime(6),
        payment_time datetime(6),
        vnpay_response_code varchar(10),
        transaction_code varchar(100),
        vnpay_txn_no varchar(100),
        method enum ('BANK_TRANSFER','CASH','VNPAY'),
        payment_status enum ('FAILED','PENDING','SUCCESS'),
        payment_type enum ('DEPOSIT','FULL','REMAINING'),
        primary key (payment_id)
    ) engine=InnoDB;

    create table platform_commission (
        commission_amount decimal(12,2) not null,
        commission_id integer not null auto_increment,
        commission_rate decimal(5,4) not null,
        commission_tier_id integer,
        court_revenue decimal(12,2) not null,
        invoice_id integer not null,
        owner_account_id integer not null,
        owner_payout decimal(12,2) not null,
        voucher_cost_owner decimal(12,2),
        voucher_cost_platform decimal(12,2),
        created_at datetime(6),
        settled_at datetime(6),
        status enum ('FAILED','PENDING','SETTLED'),
        primary key (commission_id)
    ) engine=InnoDB;

    create table product (
        category_id integer not null,
        facility_id integer not null,
        is_active bit,
        price decimal(12,2) not null,
        product_id integer not null auto_increment,
        stock_quantity integer,
        created_at datetime(6),
        rental_unit varchar(20),
        description varchar(500),
        image_path varchar(255),
        product_name varchar(255) not null,
        product_type enum ('RENTAL','SALE') not null,
        primary key (product_id)
    ) engine=InnoDB;

    create table product_category (
        category_id integer not null auto_increment,
        is_active bit,
        category_code varchar(30) not null,
        category_name varchar(255) not null,
        primary key (category_id)
    ) engine=InnoDB;

    create table review (
        account_id integer not null,
        booking_id integer not null,
        rating integer not null,
        review_id integer not null auto_increment,
        created_at datetime(6),
        comment varchar(500),
        primary key (review_id)
    ) engine=InnoDB;

    create table sport (
        default_min_duration_minutes integer not null,
        default_slot_step_minutes integer not null,
        is_active bit,
        sport_id integer not null auto_increment,
        sport_code varchar(30) not null,
        icon_path varchar(255),
        sport_name varchar(255) not null,
        primary key (sport_id)
    ) engine=InnoDB;

    create table sport_attribute (
        attribute_id integer not null auto_increment,
        is_required bit,
        sport_id integer not null,
        data_type varchar(20) not null,
        attribute_code varchar(50) not null,
        attribute_name varchar(255) not null,
        options_json LONGTEXT,
        primary key (attribute_id)
    ) engine=InnoDB;

    create table staff (
        account_id integer not null,
        facility_id integer not null,
        is_active bit,
        staff_id integer not null auto_increment,
        primary key (staff_id)
    ) engine=InnoDB;

    create table voucher (
        discount_value decimal(12,2) not null,
        is_active bit,
        issuer_account_id integer,
        max_discount_amount decimal(12,2),
        min_order_amount decimal(12,2),
        per_user_limit integer,
        usage_limit integer,
        voucher_id integer not null auto_increment,
        created_at datetime(6),
        valid_from datetime(6) not null,
        valid_to datetime(6) not null,
        code varchar(50) not null,
        description varchar(500),
        name varchar(255) not null,
        applicable_to enum ('ALL','COURT_BOOKING','PRODUCT'),
        discount_type enum ('FIXED_AMOUNT','PERCENTAGE') not null,
        issuer_type enum ('OWNER','PLATFORM') not null,
        primary key (voucher_id)
    ) engine=InnoDB;

    create table voucher_account (
        account_id integer not null,
        voucher_id integer not null
    ) engine=InnoDB;

    create table voucher_facility (
        facility_id integer not null,
        voucher_id integer not null
    ) engine=InnoDB;

    create table voucher_usage (
        account_id integer,
        booking_id integer not null,
        discount_amount decimal(12,2) not null,
        invoice_id integer not null,
        usage_id integer not null auto_increment,
        voucher_id integer not null,
        used_at datetime(6),
        liability_party varchar(10) not null,
        primary key (usage_id)
    ) engine=InnoDB;

    alter table account 
       add constraint UKq0uja26qgu1atulenwup9rxyr unique (email);

    alter table account 
       add constraint UKdgdnj692f2g5ebicy1xyc2l3w unique (phone);

    alter table blog_reaction 
       add constraint UQ_BlogReaction unique (post_id, account_id, emoji_code);

    alter table court_attribute_value 
       add constraint UKj4qa3uj33gp8awnx64rtgrn9r unique (court_id, attribute_id);

    alter table court_slot_booking 
       add constraint UKlwv7d7t524r3bxmi0h523xs9p unique (court_id, booking_date, start_time);

    alter table court_slot_booking 
       add constraint UKceeulgl6uatuliycmothuay41 unique (booking_slot_id);

    alter table customer_favorite_facility 
       add constraint UKq90nef112fe35x5g3ad9042u7 unique (account_id, facility_id);

    alter table facility_sport 
       add constraint UKdxig1mrlph0g5uqke1em81bl4 unique (facility_id, sport_id);

    alter table invoice 
       add constraint UK32ywtxrkeu1wnmivu6mlcqdid unique (booking_id);

    alter table owner_profile 
       add constraint UKhnrcmp4lyxkm7l0p5uegwmw8j unique (account_id);

    alter table password_reset_token 
       add constraint UKg0guo4k8krgpwuagos61oc06j unique (token);

    alter table platform_commission 
       add constraint UKc4yic37b8li7uaejl34er5xkd unique (invoice_id);

    alter table product_category 
       add constraint UKn4nmm8o4cegxl3lt9p48mklvj unique (category_code);

    alter table review 
       add constraint UKm685o801uf70i84jf94qq3d0b unique (booking_id);

    alter table sport 
       add constraint UK91qpqfcq5rjyanobl8fkxsch1 unique (sport_code);

    alter table staff 
       add constraint UK4uqyb8awsv3mfncjj737o7oo9 unique (account_id);

    alter table voucher 
       add constraint UKpvh1lqheshnjoekevvwla03xn unique (code);

    alter table blog_comment 
       add constraint FKtrmc5ogfx7w1u0u5uxo2nl93f 
       foreign key (author_account_id) 
       references account (account_id);

    alter table blog_comment 
       add constraint FKmbe5fcwvss5qwqlj6o4vpiwvl 
       foreign key (moderated_by_account_id) 
       references account (account_id);

    alter table blog_comment 
       add constraint FKeh1bvld0i4iq1rnw951g518l8 
       foreign key (post_id) 
       references blog_post (post_id);

    alter table blog_post 
       add constraint FK1wbk80unrcd3nis0hiwwfuwxu 
       foreign key (author_account_id) 
       references account (account_id);

    alter table blog_reaction 
       add constraint FKdh1rphxlewbgndyi4t8jw9w8j 
       foreign key (account_id) 
       references account (account_id);

    alter table blog_reaction 
       add constraint FKpxoeu71558cb4xvrhoj3g8biq 
       foreign key (post_id) 
       references blog_post (post_id);

    alter table booking 
       add constraint FK7hunottedmjhtdcvhv4sx6x4a 
       foreign key (account_id) 
       references account (account_id);

    alter table booking 
       add constraint FK6io8j4ov8vlpwc9wc37179ca1 
       foreign key (facility_id) 
       references facility (facility_id);

    alter table booking 
       add constraint FKjn3lsroa8t8h7x5sld9b0ru2u 
       foreign key (guest_id) 
       references guest (guest_id);

    alter table booking 
       add constraint FK357w452pgne0tsl2dra6ompmx 
       foreign key (staff_id) 
       references staff (staff_id);

    alter table booking_change_log 
       add constraint FKcnnm4t7shy0wf77mkjlwdgrih 
       foreign key (actor_staff_id) 
       references staff (staff_id);

    alter table booking_change_log 
       add constraint FKj14lvcomgp7lablhbkx2f7hrp 
       foreign key (booking_id) 
       references booking (booking_id);

    alter table booking_change_log 
       add constraint FK9a54ebbesyn4vthc8k8nkjm22 
       foreign key (new_court_id) 
       references court (court_id);

    alter table booking_change_log 
       add constraint FKgbqo4nhh83vqqd5dshffu19ow 
       foreign key (old_court_id) 
       references court (court_id);

    alter table booking_slot 
       add constraint FKq5d98cx093epox9u3spe2jcxw 
       foreign key (booking_id) 
       references booking (booking_id);

    alter table booking_slot 
       add constraint FK6k10g0gdoelgwmlr9gvm8otgn 
       foreign key (court_id) 
       references court (court_id);

    alter table commission_change_log 
       add constraint FKrxygn6fv1emlcpjoucw164x6n 
       foreign key (changed_by) 
       references account (account_id);

    alter table commission_change_log 
       add constraint FKhgv1p789veqk3jkudvn26jn1x 
       foreign key (new_tier_id) 
       references commission_tier (tier_id);

    alter table commission_change_log 
       add constraint FKn3poqalsmvccdgvrhivwp4dh0 
       foreign key (old_tier_id) 
       references commission_tier (tier_id);

    alter table commission_policy 
       add constraint FKo6ixex39svs4ahh6atgs08ri3 
       foreign key (updated_by) 
       references account (account_id);

    alter table commission_tier 
       add constraint FKqjmk5flqsu2y0qdv80m2ayfhm 
       foreign key (created_by) 
       references account (account_id);

    alter table court 
       add constraint FKnp646tyaa8dlfc6vgigqakww2 
       foreign key (facility_sport_id) 
       references facility_sport (facility_sport_id);

    alter table court_attribute_value 
       add constraint FKo97x0o4wlmdqn57mbavww72nh 
       foreign key (attribute_id) 
       references sport_attribute (attribute_id);

    alter table court_attribute_value 
       add constraint FKqhof5sbc0wnqlotpcqtu0esxm 
       foreign key (court_id) 
       references court (court_id);

    alter table court_schedule_exception 
       add constraint FKhqusfl723oy0nrnrk03yjpkmq 
       foreign key (court_id) 
       references court (court_id);

    alter table court_schedule_exception 
       add constraint FK9qo8kolyo4fa6ux02vxiuawo0 
       foreign key (created_by) 
       references staff (staff_id);

    alter table court_schedule_exception 
       add constraint FKl22or8cxo4a36qs25unwlkhow 
       foreign key (facility_id) 
       references facility (facility_id);

    alter table court_slot_booking 
       add constraint FK24qfggdwxfwy3vx1qcibdxpod 
       foreign key (booking_slot_id) 
       references booking_slot (booking_slot_id);

    alter table court_slot_booking 
       add constraint FKr1ilo3uku5yxc7s2ys89gt3ql 
       foreign key (court_id) 
       references court (court_id);

    alter table customer_favorite_facility 
       add constraint FKfjrnc2rrq1gnh2oc58lg23xle 
       foreign key (account_id) 
       references account (account_id);

    alter table customer_favorite_facility 
       add constraint FKk7fc6jc0vsrns8m5bltar3sya 
       foreign key (facility_id) 
       references facility (facility_id);

    alter table email_queue 
       add constraint FK97k0psx5jo5nsp5wbwq6u27uv 
       foreign key (booking_id) 
       references booking (booking_id);

    alter table facility 
       add constraint FKdn4v673vwnsce108nyf77pdfa 
       foreign key (owner_account_id) 
       references account (account_id);

    alter table facility_image 
       add constraint FKnrx8tu5s53v5tutiyv2sxw01d 
       foreign key (facility_id) 
       references facility (facility_id);

    alter table facility_price_rule 
       add constraint FKkgdipn6ecv56ew5beggx57yyo 
       foreign key (facility_sport_id) 
       references facility_sport (facility_sport_id);

    alter table facility_sport 
       add constraint FKhjl2x22yxxchd7a04o4tphvx9 
       foreign key (facility_id) 
       references facility (facility_id);

    alter table facility_sport 
       add constraint FK1t4y83xcd33kr3pj0f9nju7m6 
       foreign key (sport_id) 
       references sport (sport_id);

    alter table invoice 
       add constraint FK4jd6uuk7w0d72riyre2w14fl7 
       foreign key (booking_id) 
       references booking (booking_id);

    alter table invoice 
       add constraint FKh8mc37lrohbk7stgatwwn5doq 
       foreign key (voucher_id) 
       references voucher (voucher_id);

    alter table notification 
       add constraint FKj0b1ncedmpl7sx7t7o54t26v2 
       foreign key (account_id) 
       references account (account_id);

    alter table order_item 
       add constraint FKselo28ymemephorc1otqe7lcq 
       foreign key (booking_id) 
       references booking (booking_id);

    alter table order_item 
       add constraint FK551losx9j75ss5d6bfsqvijna 
       foreign key (product_id) 
       references product (product_id);

    alter table owner_profile 
       add constraint FKfjr685c3fuyy9iil9jnrd5v5e 
       foreign key (account_id) 
       references account (account_id);

    alter table owner_profile 
       add constraint FKj879nrm2n76uodcldu5a3jdbk 
       foreign key (approved_by) 
       references account (account_id);

    alter table payment 
       add constraint FKsb24p8f52refbb80qwp4gem9n 
       foreign key (invoice_id) 
       references invoice (invoice_id);

    alter table payment 
       add constraint FKa1s5fly67g3hxiaqa4cylyj9m 
       foreign key (staff_confirm_id) 
       references staff (staff_id);

    alter table platform_commission 
       add constraint FKbxouaeg0eaffbye01ij57255f 
       foreign key (commission_tier_id) 
       references commission_tier (tier_id);

    alter table platform_commission 
       add constraint FKlh81n7x1pc5yye31wgihvqmjl 
       foreign key (invoice_id) 
       references invoice (invoice_id);

    alter table platform_commission 
       add constraint FKdf5r0uiasavnupx2408d1be7m 
       foreign key (owner_account_id) 
       references account (account_id);

    alter table product 
       add constraint FK5cypb0k23bovo3rn1a5jqs6j4 
       foreign key (category_id) 
       references product_category (category_id);

    alter table product 
       add constraint FKlm02uo7su1hxcyflt4goj66ft 
       foreign key (facility_id) 
       references facility (facility_id);

    alter table review 
       add constraint FKbopwpfvcg5qsfrjtt9svofxg1 
       foreign key (account_id) 
       references account (account_id);

    alter table review 
       add constraint FKk4xawqohtguy5yx5nnpba6yf3 
       foreign key (booking_id) 
       references booking (booking_id);

    alter table sport_attribute 
       add constraint FKk5e7n7xud4b8r3mpb1txnvair 
       foreign key (sport_id) 
       references sport (sport_id);

    alter table staff 
       add constraint FKs9jl798sgmtrl79dm4svocvaw 
       foreign key (account_id) 
       references account (account_id);

    alter table staff 
       add constraint FKl0j7hmn56i24kta6sihk6yt7 
       foreign key (facility_id) 
       references facility (facility_id);

    alter table voucher 
       add constraint FK725xijc1ux79ti07v7e4imncx 
       foreign key (issuer_account_id) 
       references account (account_id);

    alter table voucher_account 
       add constraint FKhiiiwumq9tm0nch1hbyxn8lih 
       foreign key (account_id) 
       references account (account_id);

    alter table voucher_account 
       add constraint FK9623iyj2s1sa228y4iq9t8yjm 
       foreign key (voucher_id) 
       references voucher (voucher_id);

    alter table voucher_facility 
       add constraint FKtfu138wbm7w4vj2rx8gj9ie1b 
       foreign key (facility_id) 
       references facility (facility_id);

    alter table voucher_facility 
       add constraint FKoces5ulf2787risi4335clo2r 
       foreign key (voucher_id) 
       references voucher (voucher_id);

    alter table voucher_usage 
       add constraint FKg6fotlb4b4wakjghl8hm4mmma 
       foreign key (account_id) 
       references account (account_id);

    alter table voucher_usage 
       add constraint FKeiom45vjfhkw6yq2jwe4qiigo 
       foreign key (booking_id) 
       references booking (booking_id);

    alter table voucher_usage 
       add constraint FKc0wr3a0vbrw2wk4c5wk3ld0o1 
       foreign key (invoice_id) 
       references invoice (invoice_id);

    alter table voucher_usage 
       add constraint FKbrrttecc2rpi70ouyw9rjrv1i 
       foreign key (voucher_id) 
       references voucher (voucher_id);
