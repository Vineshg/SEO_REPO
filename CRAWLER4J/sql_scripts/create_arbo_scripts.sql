CREATE TABLE IF NOT EXISTS ARBOCRAWL_RESULTS (
    URL TEXT,
    WHOLE_TEXT TEXT,
    TITLE TEXT,
    H1 TEXT,
    SHORT_DESCRIPTION TEXT,
    STATUS_CODE INT,
    DEPTH INT,
    OUTLINKS_SIZE INT,
    INLINKS_SIZE INT,
    NB_BREADCRUMBS INT,
    NB_AGGREGATED_RATINGS INT,
    NB_RATINGS_VALUES INT,
    NB_PRICES INT,
    NB_AVAILABILITIES INT,
    NB_REVIEWS INT,
    NB_REVIEWS_COUNT INT,
    NB_IMAGES INT,
    NB_SEARCH_IN_URL INT,
    NB_ADD_IN_TEXT INT,
    NB_FILTER_IN_TEXT INT,
    NB_SEARCH_IN_TEXT INT,
    NB_GUIDE_ACHAT_IN_TEXT INT,
    NB_PRODUCT_INFO_IN_TEXT INT,
    NB_LIVRAISON_IN_TEXT INT,
    NB_GARANTIES_IN_TEXT INT,
    NB_PRODUITS_SIMILAIRES_IN_TEXT INT,
    NB_IMAGES_TEXT INT,
    WIDTH_AVERAGE REAL,
    HEIGHT_AVERAGE REAL,
    PAGE_RANK REAL,
    PAGE_TYPE TEXT,
    SEMANTIC_HITS TEXT,
    CONCURRENT_NAME VARCHAR(50),
    LAST_UPDATE DATE
) TABLESPACE mydbspace;

CREATE INDEX ON ARBOCRAWL_RESULTS (url);

INSERT INTO ARBOCRAWL_RESULTS
(URL, WHOLE_TEXT, TITLE, H1, SHORT_DESCRIPTION, STATUS_CODE, DEPTH, OUTLINKS_SIZE, INLINKS_SIZE, NB_BREADCRUMBS, NB_AGGREGATED_RATINGS, NB_RATINGS_VALUES, NB_PRICES, NB_AVAILABILITIES, NB_REVIEWS, NB_REVIEWS_COUNT, NB_IMAGES INT,SEMANTIC_HITS,CONCURRENT_NAME,LAST_UPDATE)"
VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)

