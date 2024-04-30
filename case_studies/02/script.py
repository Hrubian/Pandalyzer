import pandas as pd


def get_country_dataframe(country):
    if country == "Germany":
        return pd.read_csv("de.csv")
    elif country == "Austria":
        return pd.read_csv("au.csv")
    else:
        return pd.read_csv("world.csv")


def get_dataframe_from_user():
    country = input("Select a country: ")
    return get_country_dataframe(country)


user_df = get_dataframe_from_user()
user_df[["germany_specific_column"]].to_csv("output.csv")
