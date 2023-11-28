import argparse
import sys
import ast
from ast2json import ast2json
import json

parser = argparse.ArgumentParser()

parser.add_argument("-o", "--output", default=None, type=str, help="The output filename")
parser.add_argument("-i", "--input", default=None, type=str, help="The input filename")


def parse(input_stream, output_stream):
    output_stream.write(json.dumps(ast2json(ast.parse(input_stream.read())), indent=4))
    return


if __name__ == '__main__':
    print("Starting", file=sys.stderr) # todo remove
    # args = parser.parse_args([] if "__file__" not in globals() else None)
    args = parser.parse_args()
    print("Got args", args, file=sys.stderr)
    if args.input is None:
        input_stream = sys.stdin
    else:
        input_stream = open(args.input, "r")

    if args.output is None:
        output_stream = sys.stdout
    else:
        output_stream = open(args.output, "w")

    parse(input_stream, output_stream)
