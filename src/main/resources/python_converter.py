# Copyright (c) 2013, Laurent Peuch <cortex@worlddomination.be>
#
# All rights reserved.
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
# * Redistributions of source code must retain the above copyright
#   notice, this list of conditions and the following disclaimer.
# * Redistributions in binary form must reproduce the above copyright
#   notice, this list of conditions and the following disclaimer in the
#   documentation and/or other materials provided with the distribution.
# * Neither the name of the University of California, Berkeley nor the
#   names of its contributors may be used to endorse or promote products
#   derived from this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
# EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
# WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
# DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
# (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
# LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
# ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
# (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
# SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
#
# Specific code snippets (BUILTIN_PURE, BUILTIN_BYTES, BUILTIN_STR, decode_str, decode_bytes, get_value, ast_to_json)
# were copied (and modified) from the ast2json library, which can be found in the following github repository:
# https://github.com/YoloSwagTeam/ast2json

import argparse
import sys
import ast
from _ast import AST, Constant
import json
import warnings

warnings.filterwarnings("ignore", category=DeprecationWarning)


parser = argparse.ArgumentParser()

parser.add_argument("-o", "--output", default=None, type=str, help="The output filename")
parser.add_argument("-i", "--input", default=None, type=str, help="The input filename")

BUILTIN_PURE = (int, float, bool)
BUILTIN_BYTES = (bytearray, bytes)
BUILTIN_STR = (str)


def decode_str(value):
    return value


def decode_bytes(value):
    try:
        return value.decode('utf-8')
    except:
        return codecs.getencoder('hex_codec')(value)[0].decode('utf-8')


def get_node_type(node):
    if isinstance(node, Constant):
        val = getattr(node, "value")
        if isinstance(val, str):
            return "StringConstant"
        elif isinstance(val, bool):
            return "BoolConstant"
        elif isinstance(val, int):
            return "IntConstant"
        elif isinstance(val, float):
            return "FloatConstant"
        elif val is None:
            return "NoneConstant"
        else:
            raise Exception("Unhandled Constant case type" + str(type(val)))
    else:
        return node.__class__.__name__

def get_value(attr_value):
    if attr_value is None:
        return attr_value
    if isinstance(attr_value, BUILTIN_PURE):
        return attr_value
    if isinstance(attr_value, BUILTIN_BYTES):
        return decode_bytes(attr_value)
    if isinstance(attr_value, BUILTIN_STR):
        return decode_str(attr_value)
    if isinstance(attr_value, complex):
        return str(attr_value)
    if isinstance(attr_value, list):
        return [get_value(x) for x in attr_value]
    if isinstance(attr_value, AST):
        return ast_to_json(attr_value)
    if isinstance(attr_value, type(Ellipsis)):
        return '...'
    else:
        raise Exception("unknown case for '%s' of type '%s'" % (attr_value, type(attr_value)))


def ast_to_json(node):
    json = {}
    json["_type"] = get_node_type(node)
    for attribute in dir(node):
        if attribute.startswith('_'):
            continue
        json[attribute] = get_value(getattr(node, attribute))
    return json


def parse(input_stream, output_stream):
    output_stream.write(json.dumps(ast_to_json(ast.parse(input_stream.read())), indent=4))
    return


if __name__ == '__main__':
    args = parser.parse_args()
    if args.input is None:
        input_stream = sys.stdin
    else:
        input_stream = open(args.input, "r")

    if args.output is None:
        output_stream = sys.stdout
    else:
        output_stream = open(args.output, "w")

    parse(input_stream, output_stream)
