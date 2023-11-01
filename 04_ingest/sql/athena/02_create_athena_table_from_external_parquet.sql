CREATE EXTERNAL TABLE IF NOT EXISTS amazon_reviews_parquet(
         marketplace string,
         customer_id string,
         review_id string,
         product_id string,
         product_parent string,
         product_title string,
         star_rating int,
         helpful_votes int,
         total_votes int,
         vine string,
         verified_purchase string,
         review_headline string,
         review_body string,
         review_date bigint,
         year int
) PARTITIONED BY (
        product_category string
)
ROW FORMAT SERDE 'org.apache.hadoop.hive.ql.io.parquet.serde.ParquetHiveSerDe' STORED AS INPUTFORMAT 'org.apache.hadoop.hive.ql.io.parquet.MapredParquetInputFormat' OUTPUTFORMAT 'org.apache.hadoop.hive.ql.io.parquet.MapredParquetOutputFormat' LOCATION 's3://dsoaws/amazon-reviews-pds/parquet/'
