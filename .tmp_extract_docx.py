from zipfile import ZipFile
from xml.etree import ElementTree as ET

path = r'c:\Users\Sept_thirteen\Desktop\WeChat-Mini-Program\md\商业计划书模板.docx'
ns = {'w': 'http://schemas.openxmlformats.org/wordprocessingml/2006/main'}

with ZipFile(path) as z:
    xml = z.read('word/document.xml')
root = ET.fromstring(xml)
texts = []
for p in root.findall('.//w:p', ns):
    ts = [t.text for t in p.findall('.//w:t', ns) if t.text]
    if ts:
        texts.append(''.join(ts))

print('\n'.join(texts))
