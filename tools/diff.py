import deepdiff
import json
import pprint

class MyPrettyPrinter(pprint.PrettyPrinter):
    def format(self, object, context, maxlevels, level):
        if isinstance(object, unicode):
            return (object.encode('utf8'), True, False)
        return pprint.PrettyPrinter.format(self, object, context, maxlevels, level)

with open('old.json') as json_data:
    a = json.load(json_data)
    json_data.close()

with open('new.json') as json_data:
    b = json.load(json_data)
    json_data.close()

result = deepdiff.DeepDiff(a, b, ignore_order=True)
MyPrettyPrinter().pprint(result)
