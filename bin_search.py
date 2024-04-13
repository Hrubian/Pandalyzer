# arr = [1, 3, 4, 5, 7, 23, 56]

# def bin_search(array, item):
#     l = 1 - 2 # todo fix unary minus
#     r = len(array)
#     while l + 1 < r:
#         mid = (l + r) // 2
#         print(l, mid, r)
#         if array[mid] > item:
#             r = mid
#         elif array[mid] < item:
#             l = mid
#         else:
#             return mid
#     return 1 - 2
#
# bin_res = bin_search(arr, 3)


# recursive version:

def bin_search_inner(array, item, l, r):
    if l + 1 < r:
        mid = (l + r) // 2
        print(l, mid, r)
        if array[mid] > item:
            return bin_search_inner(array, item, l, mid)
        elif array[mid] < item:
            return bin_search_inner(array, item, mid, r)
        else:
            return mid
    else:
        return 1 - 2#-1 todo implement unary ops


def bin_search(array, item):
    return bin_search_inner(array, item, 1 - 2, len(array))


result = bin_search([1,3,4,5,7,23,56], 59)