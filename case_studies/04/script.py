import pandas as pd

df = pd.DataFrame({
    "string_column": [input("First string: "), input("Second string: "), input("Third string: ")],
    "int_column": [int(input("First int: ")), int(input("Second int: ")), int(input("Third int: "))]
})

print(df[input("What column do you want to see? ")])

df.insert(2, "note", "User inserted string: " + df["string_column"])

df.to_csv("output.csv")
