"""
Created on May 3, 2014

@author: Ronald Haentjens Dekker
"""
from collatex.core_classes import VariantGraph, Witness, join, AlignmentTable, Row, WordPunctuationTokenizer
from collatex.extended_suffix_array import ExtendedSuffixArray
from collatex.exceptions import UnsupportedError
from collatex.experimental_astar_aligner import ExperimentalAstarAligner
from collatex.linsuffarr import SuffixArray, UNIT_BYTE
from ClusterShell.RangeSet import RangeSet
import json
from collatex.edit_graph_aligner import EditGraphAligner
from collatex.display_module import display_alignment_table_as_HTML, visualizeTableVerticallyWithColors
from collatex.display_module import display_variant_graph_as_SVG

# Valid options for output are:
# "table" for the alignment table (default)
# "graph" for the variant graph
# "json" for the alignment table exported as JSON
def collate(collation, output="table", layout="horizontal", segmentation=True, near_match=False, astar=False, detect_transpositions=False, debug_scores=False, properties_filter=None):
    if not astar:
        algorithm = EditGraphAligner(collation, near_match=near_match, detect_transpositions=detect_transpositions, debug_scores=debug_scores, properties_filter=properties_filter)
    else:
        algorithm = ExperimentalAstarAligner(collation, near_match=near_match, debug_scores=debug_scores)

    # build graph
    graph = VariantGraph()
    algorithm.collate(graph, collation)
    # join parallel segments
    if segmentation:
        join(graph)
    # check which output format is requested: graph or table
    if output == "svg":
        return display_variant_graph_as_SVG(graph)
    if output=="graph": 
        return graph
    # create alignment table
    table = AlignmentTable(collation, graph, layout)
    if output == "json":
        return export_alignment_table_as_json(table)
    if output == "html":
        return display_alignment_table_as_HTML(table)
    if output == "html2":
        return visualizeTableVerticallyWithColors(table, collation)
    if output == "table":
        return table
    else:
        raise Exception("Unknown output type: "+output)
    
def collate_pretokenized_json(json, output='table', layout='horizontal', **kwargs):
    # Takes the same arguments as collate() above
    if output not in ['json', 'table', 'html2', 'html', 'svg']:
        raise UnsupportedError("Output type " + output + " not supported for pretokenized collation")

    collation = Collation()
    for witness in json["witnesses"]:
        collation.add_witness(witness)
    return collate(collation,output=output,**kwargs)

def export_alignment_table_as_json(table, indent=None, status=False):
    json_output = {}
    json_output["table"]=[]
    sigli = []
    for row in table.rows:
        sigli.append(row.header)
        json_output["table"].append([[cell.token_data] for cell in row.cells])
    json_output["witnesses"] = sigli
    if status:
        variant_status = []
        for column in table.columns:
            variant_status.append(column.variant)
        json_output["status"] = variant_status
    return json.dumps(json_output, sort_keys=True, indent=indent)

class Collation(object):

    @classmethod
    def create_from_dict(cls, data, limit=None):
        witnesses = data["witnesses"]
        collation = Collation()
        for witness in witnesses[:limit]:
            # generate collation object from json_data
            collation.add_witness(witness)
        return collation

    @classmethod
    # json_data can be a string or a file
    def create_from_json(cls, json_data):
        data = json.load(json_data)
        collation = cls.create_from_dict(data)
        return collation

    def __init__(self):
        self.witnesses = []

    def add_witness(self, witnessdata):
        witness = Witness(witnessdata)
        self.witnesses.append(witness)

    def add_plain_witness(self, sigil, content):
        return self.add_witness({'id':sigil, 'content':content})



