CREATE TABLE IF NOT EXISTS SOLR_VS_EXALEAD_PRODUCT_LIST (
    ID SERIAL PRIMARY KEY  NOT NULL,
    URL TEXT,
    TO_FETCH BOOLEAN,
    STATUS_SOLR INT,
    MEILLEUR_VENTE_PRODUCTS_SOLR TEXT,
    PAGE_BODY_PRODUCTS_SOLR TEXT,
    PAGE2_BODY_PRODUCTS_SOLR TEXT,
    NUMBER_OF_PRODUCTS_SOLR TEXT,
    FACETTES_SOLR TEXT,
    GEOLOC_SOLR TEXT,
    STATUS_EXALEAD INT,
    MEILLEUR_VENTE_PRODUCTS_EXALEAD TEXT,
    PAGE_BODY_PRODUCTS_EXALEAD TEXT,
    PAGE2_BODY_PRODUCTS_EXALEAD TEXT,
    NUMBER_OF_PRODUCTS_EXALEAD TEXT,
    FACETTES_EXALEAD TEXT,
    GEOLOC_EXALEAD TEXT,
    STATUS_COMPARISON INT,
    MEILLEUR_VENTE_PRODUCTS_COMPARISON INT,
    PAGE_BODY_PRODUCTS_COMPARISON INT,
    PAGE2_BODY_PRODUCTS_COMPARISON INT,
    NUMBER_OF_PRODUCTS_COMPARISON INT,
    FACETTES_COMPARISON INT,
    GEOLOC_COMPARISON INT
) TABLESPACE mydbspace;