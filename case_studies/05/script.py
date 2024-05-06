import pandas as pd

all_strings_df = pd.read_csv("all_strings.csv")
first_string_df = pd.read_csv("first_string.csv")
first_int_df = pd.read_csv("first_int.csv")
all_different_df = pd.read_csv("all_different.csv")

pass_df1 = first_string_df.groupby("col1").mean()
fail_df1 = first_string_df.groupby("col2").mean()

pass_df2 = all_strings_df.groupby("str1").count()["str2"] + 3
fail_df2 = all_strings_df.groupby("str1").count()["str2"] + "hello"

pass_df3 = all_different_df.groupby("bool_col").sum()
fail_df3 = all_different_df.groupby("str_col").sum() # todo this does not fail :)

pass_df4 = first_int_df.groupby(["col2", "col3"]).mean()
fail_df4 = first_int_df.groupby("col3").mean()
