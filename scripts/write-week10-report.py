"""Fill only the supplied report's table slots; retain other ZIP parts byte for byte."""
from pathlib import Path
from zipfile import ZipFile
from copy import deepcopy
from hashlib import sha256
from lxml import etree as ET
import json
import xml.etree.ElementTree as XML

ROOT = Path(__file__).resolve().parents[1]
SOURCE = Path(r"C:\Users\izim5\Downloads\소프트웨어특강2_Portfolio_Upgrade_주간실습보고서_양식.docx")
OUT = ROOT / "docs/week10/10주차_주간실습보고서_설명우선본.docx"
QA = ROOT / "evidence/week10/report-qa"
QA.mkdir(parents=True, exist_ok=True)
totals = dict(tests=0, failures=0, errors=0, skipped=0)
for file in (ROOT / "build/test-results/test").glob("TEST-*.xml"):
    suite = XML.parse(file).getroot()
    for key in totals:
        totals[key] += int(suite.get(key, 0))
test_label = f"{totals['tests']}개 실행, 실패 {totals['failures'] + totals['errors']}개"
rows = {
  1: [["이번 주 핵심 목표", "현재 기능을 분석하고 문제를 실험으로 확인", "테스트 환경 구축·문제 재현 완료. 속도 측정은 미실시", "☐ 완료  ☑ 부분완료  ☐ 미수행"]],
  2: [
    ["1", "기존 코드를 보존하고 별도 작업 공간 생성", "작업 브랜치\nrefactor/baseline", "원래 코드와 비교 가능"],
    ["2", "지원·발표 등 기능 4개의 동작과 테스트 상황 정리", "E1 기능 설명·실험 계획 문서", "입력·응답·접근 권한 정리"],
    ["3", "실험용 DB와 가짜 알림 서버 구축", "E2 테스트 코드", "알림 실패·지연을 조절 가능"],
    ["4", "중복 요청·알림 실패·동시 처리 실험", "E3 실행 결과·원본 기록", "중복 등록·데이터 불일치 발견"],
    ["5", "실험 결과와 남은 작업을 보고서로 정리", "E4 결과 해설 문서", "실측 결과와 추정 구분"],
  ],
  3: [
    ["프로그램 실행 준비", "실행 파일 생성·기존 검사 5개 실행", "실행 파일 생성 성공\n기존 검사 5개 통과", "E3 기존 실행 기록"],
    ["문제 상황 재현", "기존 5개 + 새 검사 78개", test_label+"\n결함 해결을 뜻하지 않음", "E3 전체 검사 결과"],
    ["동일 발표 동시 요청", "한 지원자에게 요청 10개를 동시에 전송·10회 반복", "매회 회원 10행·알림 요청 10회", "E3 동시 발표 결과"],
  ],
  4: [
    ["계획 작성", "기능 분석·실험 항목 제안", "사용자가 작업 범위 지정", "문제 확인을 먼저 수행", "실제 코드와 대조"],
    ["테스트 작성·실행", "실험 환경·검사 코드 작성", "개인별 코드 검토는 미기록", "AI가 오류 수정 후 재실행", "83개 검사 결과 보존"],
    ["결과 정리", "발견한 문제와 증거 설명", "제출 전 본인 확인 필요", "미측정 성능은 성과에서 제외", "원본 기록과 비교 가능"],
  ],
  5: [
    ["검사 코드가 실행되지 않음", "한글 폴더 경로 영향 추정", "코드 수정 / 경로 별칭", "같은 폴더를 W:로 연결", "실행 성공. 원인은 추가 확인 필요"],
    ["가짜 서버 대신 실제 알림 서버에 요청", "테스트 주소 설정이 덮어써짐", "기본 주소 차단·설정 순서 수정", "가짜 인증이 거절된 뒤 중단·수정", "이후 가짜 서버의 수신 기록 확인"],
  ],
  6: [
    ["개선 전 속도 측정", "50명·100명 발표의 응답 시간·처리량 측정", "측정 조건·수치·원본 기록"],
    ["알림·DB 불일치 개선 설계", "결과 저장과 알림 전송을 분리하고 중복 등록 방지", "설계 이유·수정 코드"],
    ["발표 결과 보호", "발표 후 결과 수정·중복 전송 차단", "개선 전후 같은 실험 비교"],
  ],
}
ns = {"w": "http://schemas.openxmlformats.org/wordprocessingml/2006/main"}
tag = lambda name: "{" + ns["w"] + "}" + name
with ZipFile(SOURCE) as source:
    root = ET.fromstring(source.read("word/document.xml"))
    tables = root.findall(".//w:body/w:tbl", ns)
    def set_cell(cell, text):
        paragraphs = cell.findall("w:p", ns)
        p = paragraphs[0]
        # Preserve paragraph/run styling and all cell geometry.
        original_run = p.find("w:r", ns)
        rpr = original_run.find("w:rPr", ns) if original_run is not None else None
        for child in list(p):
            if child.tag != tag("pPr"):
                p.remove(child)
        for i, line in enumerate(text.split("\n")):
            run = ET.SubElement(p, tag("r"))
            if rpr is not None:
                run.append(deepcopy(rpr))
            if i:
                ET.SubElement(run, tag("br"))
            ET.SubElement(run, tag("t")).text = line
        for extra in paragraphs[1:]:
            cell.remove(extra)
    for table_index, records in rows.items():
        target_rows = tables[table_index].findall("w:tr", ns)[1:]
        for row, record in zip(target_rows, records):
            for cell, value in zip(row.findall("w:tc", ns), record):
                set_cell(cell, value)
    # No claim that these activities took exactly the template's two-hour class slot.
    header = tables[1].findall("w:tr", ns)[0].findall("w:tc", ns)[2]
    set_cell(header, "현재까지 실제 수행")
    def paragraph(text, heading=False, page_break=False):
        p=ET.Element(tag("p")); props=ET.SubElement(p,tag("pPr"))
        if page_break: ET.SubElement(props,tag("pageBreakBefore"))
        spacing=ET.SubElement(props,tag("spacing"));spacing.set(tag("after"),"100")
        run=ET.SubElement(p,tag("r")); style=ET.SubElement(run,tag("rPr"))
        fonts=ET.SubElement(style,tag("rFonts"));fonts.set(tag("ascii"),"맑은 고딕");fonts.set(tag("eastAsia"),"맑은 고딕")
        size=ET.SubElement(style,tag("sz"));size.set(tag("val"),"26" if heading else "21")
        color=ET.SubElement(style,tag("color"));color.set(tag("val"),"000000")
        if heading: ET.SubElement(style,tag("b"))
        ET.SubElement(run,tag("t")).text=text
        return p
    intro=paragraph("동구라미는 학생이 동아리에 지원하고, 회장이 합격·불합격을 발표하는 서비스다. 이번 주에는 기존 기능의 문제를 실험으로 확인했다. 문제 수정과 속도 측정은 아직 하지 않았다.")
    tables[0].addnext(intro)
    body=root.find("w:body",ns)
    appendix=[
        ("프로젝트 소개와 실험 결과 안내",True),
        ("1 무엇을 확인했는가",True),
        ("동구라미는 학생의 동아리 지원을 돕는 서비스다. 회장이 결과를 발표하면 합격자를 회원으로 등록하고 휴대폰 알림을 보낸다. 여러 요청이 겹치거나 알림 서버가 실패할 때도 결과가 일치하는지 확인했다.",False),
        ("2 실제로 발견한 문제",True),
        ("① 중복 등록: 화면 반응이 느려 발표 버튼을 다시 누르면 같은 요청이 반복될 수 있다. 이를 확인하려고 한 지원자의 발표 요청 10개를 동시에 보냈더니 회원 정보가 10행 생기고 알림도 10회 요청됐다. 10회 반복 모두 재현됐다.",False),
        ("② 지원서 제출 오류: 같은 지원서를 동시에 10번 제출했을 때 저장은 1건으로 제한됐다. 그러나 나머지 9개 요청에는 서버 내부 오류(500)가 반환됐다. 중복 저장 방지와 사용자 오류 안내를 따로 개선해야 한다.",False),
        ("③ 알림과 저장 결과 불일치: 첫 알림을 성공 처리한 뒤 다음 알림을 실패시키자, 저장한 발표 결과와 신규 회원 정보가 취소됐다. 이미 처리한 첫 알림은 취소되지 않았다. 여기서 성공은 가짜 서버가 요청을 받았다는 뜻이며 실제 휴대폰 수신은 아니다.",False),
        ("④ 결과 변경이 사라짐: 합격 발표와 불합격 변경을 동시에 실행하자 두 요청 모두 성공 응답을 받았지만 최종 상태는 합격으로 남았다. 사용자는 불합격으로 변경됐다고 이해할 수 있으므로 결과 보호가 필요하다.",False),
        ("3 검사 83개 통과의 의미",True),
        ("기존 검사 5개와 새 검사 78개를 실행했다. 새 검사에는 현재 결함이 발생하는지 확인하는 코드도 있다. 따라서 83개 통과는 실험이 예상대로 실행됐다는 뜻이다. 결함을 해결했거나 서비스 전체가 안전하다는 뜻은 아니다.",False),
        ("4 증거 자료는 어디에서 보는가",True),
        ("아래 경로는 서버 저장소 폴더를 기준으로 한다. 실행 기록(JSON)은 응답·DB 결과·알림 요청을 저장한 원본 파일이다. 발표에서는 전체 파일 대신 핵심 숫자를 캡처하고 원본을 함께 제출한다.",False),
        ("E1 기능·실험 설명: docs/week10/api-contract-and-scenarios.md 및 detailed-reproduction-scenarios.md",False),
        ("E2 검사 코드: src/test/java/com/USWCicrcleLink/server/baseline/BaselineReproductionTest.java",False),
        ("E3 전체 결과: evidence/week10/test-run/reproduction-final.log. 중복 등록: evidence/week10/reproduction/PUB-06-1.json의 members=10과 알림 기록 10개. 제출 오류: 같은 폴더의 APPLY-03.json. 알림 실패: ERR-500.json.",False),
        ("E4 결과 해설: docs/week10/reproduction-results.md. 먼저 이 문서를 읽고 원본 기록과 비교하면 된다.",False),
        ("수행 구분: 사용자는 작업 범위·브랜치명·발표 후 수정 금지 방향을 결정했다. 테스트 코드 작성·실행과 문서 초안은 AI 도구가 수행했다. 본인의 코드 검토·재실행 경험은 제출 전에 직접 확인해 기록해야 한다.",False),
    ]
    # Put the explanation before the form, rather than in a bottom appendix.
    first_form_paragraph=body.find("w:p",ns)
    first_props=first_form_paragraph.find("w:pPr",ns)
    if first_props is None:
        first_props=ET.Element(tag("pPr"));first_form_paragraph.insert(0,first_props)
    ET.SubElement(first_props,tag("pageBreakBefore"))
    for index,(text,heading) in enumerate(appendix):
        body.insert(index,paragraph(text,heading))
    with ZipFile(OUT, "w") as result:
        for info in source.infolist():
            data = ET.tostring(root, xml_declaration=True, encoding="UTF-8", standalone=True) if info.filename == "word/document.xml" else source.read(info.filename)
            result.writestr(info, data)
with ZipFile(SOURCE) as source, ZipFile(OUT) as result:
    changed = [name for name in source.namelist() if source.read(name) != result.read(name)]
assert changed == ["word/document.xml"], changed
(QA / "artifact.md").write_text(
    "# Template contract\nReference: " + str(SOURCE) + "\nSHA256: " + sha256(SOURCE.read_bytes()).hexdigest()
    + "\nA4 portrait, 0.4333 inch top/bottom margins, 1 section, 2 reference pages.\n"
    + "Retain styles, page system, metadata, headers/footers and all non-document ZIP parts.\n"
    + "Slots: body tables 1–6, non-header rows; table 1 header cell 2 relabelled for timing accuracy.\n"
    + "Authorized revision: reader guide moved before the original form; duplicate-request example explained inline. Original form begins on a new page.\n"
    + "Keep table widths, borders, fonts and all paragraph/run properties. Allow natural row growth.\n"
    + "AI personal judgement slots remain explicitly unconfirmed; self-checks remain unmarked.\n"
    + "Reference renders: reference/page-1.png, reference/page-2.png.\n"
    + "Packaged renderer unavailable: LibreOffice missing. Fallback: Word hidden PDF export + bundled Poppler PNG.\n"
    + "Changed package parts: word/document.xml only.\n", encoding="utf-8")
print(OUT)
