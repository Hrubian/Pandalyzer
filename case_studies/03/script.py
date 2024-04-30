import pandas as pd

tuesday_df = pd.read_csv("30_04_2024_production.csv")
wednesday_df = pd.read_csv("31_04_2024_production.csv")

tuesday_df.insert(0, "day", 30)
wednesday_df.insert(0, "day", 31)

agg_df = pd.concat([tuesday_df, wednesday_df])

low_production_df = agg_df[agg_df["production"] < 400]

low_production_df.to_csv("aggregate_production.csv")
