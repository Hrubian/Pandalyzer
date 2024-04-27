# def bin_search_inner(array, item, l, r):
#     if l + 1 < r:
#         #mid = (l + r) // 2
#         mid = l + (r - l) // 2
#         if array[mid] > item:
#             return bin_search_inner(array, item, l, mid)
#         else:
#             return bin_search_inner(array, item, mid, r)
#     else:
#         if len(array) > 0 and array[l] == item:
#             return l
#         else:
#             return -1
#
#
# def bin_search(array, item):
#     return bin_search_inner(array, item, 0, len(array))
#
#
# not_found = bin_search([1,3,4,5,7,23,56], 0)
# empty = bin_search([], 1)

import pandas as pd

df = pd.read_csv("test.csv")
# df.groupby("ahoj").mean()
df2 = df.rename({"column1": "col2", "column2": "col2"})
df.groupby("column2").mean()
print(df["column1"])
print(df["column3"])