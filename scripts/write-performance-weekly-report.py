"""Append measured performance to the existing weekly form, preserving its original."""
import argparse
import copy
import json
from pathlib import Path
from zipfile import ZipFile
from lxml import etree as E


def valid_report_rows(rows):
    expected = {f'n{count}-vu1-r{repeat}' for count in (50, 100) for repeat in (1, 2, 3)}
    return (len(rows) == 6 and {row['run_id'] for row in rows} == expected and
            all(row['passed'] and row['minimum_duration_met'] and row['attempts'] == 30 and row['vus'] == 1 and
                row['applicants'] in (50, 100) and row['run_id'].startswith(f"n{row['applicants']}-vu1-r") and
                row['http_failures'] == row['business_failures'] == row['timeouts'] == 0 for row in rows))


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('directory', type=Path)
    parser.add_argument('--interrupted', action='store_true', help='Record the known five verified runs and final interrupted attempt honestly.')
    parser.add_argument('--comparison-summary', type=Path, help='Six verified rows with explicit campaign provenance, old 50 + new 100.')
    parser.add_argument('--output', type=Path, help='Optional separate output document path.')
    args = parser.parse_args()
    root = Path(__file__).resolve().parents[1]
    rows = json.loads((args.comparison_summary or (args.directory / 'summary.json')).read_text(encoding='utf-8'))
    if args.comparison_summary:
        audit = json.loads((args.directory / 'power-log-audit.json').read_text(encoding='utf-8-sig'))
        if not audit['querySucceeded'] or audit['events']:
            raise SystemExit('Successful empty power-log audit required for the retry report')
    partial = args.interrupted
    if partial:
        expected = {'n50-vu1-r1','n50-vu1-r2','n50-vu1-r3','n100-vu1-r1','n100-vu1-r2'}
        failure = json.loads((args.directory / 'n100-vu1-r3/failure.json').read_text(encoding='utf-8'))
        if len(rows) != 5 or {r['run_id'] for r in rows} != expected or not all(r['passed'] and r['attempts'] == 30 for r in rows) or failure['attempts'] != 30 or failure['httpFailures'] != 1 or failure['dbVerified']:
            raise SystemExit('Interrupted report does not match the recorded five verified runs plus one unverified timeout run.')
    elif not valid_report_rows(rows):
        raise SystemExit('Report template requires exactly 50/100 applicants, VU1, 3 runs each, 30 requests each, all verified without failure.')
    source = root / 'docs/week10/10주차_주간실습보고서_수정후결과링크.docx'
    target = root / ('docs/week10/10주차_주간실습보고서_성능측정_중간.docx' if partial else ('docs/week10/10주차_주간실습보고서_성능재측정완료.docx' if args.comparison_summary else 'docs/week10/10주차_주간실습보고서_성능측정.docx'))
    if args.output:
        target = args.output.resolve()
    ns = {'w': 'http://schemas.openxmlformats.org/wordprocessingml/2006/main'}
    q = lambda name: '{' + ns['w'] + '}' + name
    p95 = {}
    for count in (50, 100):
        values = [row['p95_ms'] / 1000 for row in rows if row['applicants'] == count]
        p95[count] = f'{min(values):.2f}~{max(values):.2f}초'
    with ZipFile(source) as original:
        doc = E.fromstring(original.read('word/document.xml'))
        tables = doc.findall('.//w:body/w:tbl', ns)

        def set_cell(table, row, col, text):
            cell = tables[table].findall('w:tr', ns)[row].findall('w:tc', ns)[col]
            first = cell.find('w:p', ns)
            for child in list(first):
                if child.tag != q('pPr'):
                    first.remove(child)
            for extra in cell.findall('w:p', ns)[1:]:
                cell.remove(extra)
            E.SubElement(E.SubElement(first, q('r')), q('t')).text = text

        def add_row(table, cells):
            new = copy.deepcopy(tables[table].findall('w:tr', ns)[-1])
            tables[table].append(new)
            index = len(tables[table].findall('w:tr', ns)) - 1
            for col, text in enumerate(cells):
                set_cell(table, index, col, text)

        set_cell(0, 1, 5, '26.11.03~06' if partial or args.comparison_summary else '26.11.03~05')
        add_row(2, ['3', 'AI가 현재 발표 API의 50명·100명 처리 시간을 측정. 조건별 3회, 실행당 새 발표 30개',
                    '성능 결과: docs/week10/performance-baseline.md\n원시 JSON·CSV: evidence/week10/performance/' + args.directory.name + '\n신규 자료는 로컬 보관, 공개 링크 미발행',
                    (f'정상 실행 p95: 50명 {p95[50]}(3회), 100명 {p95[100]}(2회). 마지막 실행 1건 타임아웃. 기준선 미완료.' if partial else f'실행별 p95: 50명 {p95[50]}, 100명 {p95[100]}. 실패 0/180건.')])
        if args.comparison_summary:
            set_cell(2, 3, 1, 'AI가 50명·100명 발표 응답 시간을 측정. 조건별 3회, 실행당 새 발표 요청 30개')
            set_cell(2, 3, 2, 'performance-baseline.md에 원시 결과 출처 기록. 50명은 11/5, 100명은 11/6 측정. 새 자료는 로컬 보관, 공개 링크 미발행')
            set_cell(2, 3, 3, f'기준선으로 채택한 6회 p95: 50명 {p95[50]}, 100명 {p95[100]}. 각30건·실패0/180')
        for col, text in enumerate(['신규 지원 결과 발표 성능', '동시 요청 1개, FCM 대역 지연 200ms. 조건별 워밍업 제외 후 30개 요청씩 3회',
                                    ('5회 검증 일치. 마지막 30건 중 1건 타임아웃, DB·FCM 검증 미완료. 휴대폰 수신 검사는 아님' if partial else '6회 모두 공개·회원·알림 요청 수 일치. 실제 휴대폰 수신 검사는 아님'),
                                    'performance-baseline.md 및 실행별 verification.json·raw.jsonl']):
            set_cell(3, 2, col, text)
        add_row(4, ['성능 측정', '전용 DB·가짜 알림 서버·k6 도구 작성 및 50명·100명 실험 실행',
                    '설명 이해. 직접 코드 확인·재실행은 아직 안 함', '본인 판단 미기록', ('정상5회 검증. 마지막 실패 원인 분리·재측정 필요' if partial else 'AI 검증 완료. 본인 재실행 필요')])
        for col, text in enumerate(['신규 발표 시 알림 응답을 기다려 API 응답이 지연됨',
                                    '현재 코드는 한 명씩 외부 알림을 보내고 응답을 기다림', '향후 알림 비동기 처리 검토',
                                    '이번에는 현재 성능을 측정. 개선 방식에 대한 본인 판단은 아직 미기록',
                                    f'50명 p95 {p95[50]}, 100명 {p95[100]}. Google 인증 제외한 대역 환경 수치']):
            set_cell(5, 2, col, text)
        if partial:
            add_row(5, ['마지막 100명 실행에서 요청 1건 타임아웃', '약14시간 실행 지연·시계 변화 경고. 절전 또는 앱 원인으로 확정하지 않음',
                        '정상 수치와 실패 기록을 분리해 보존', '실패를 삭제하지 않고 기준선 미완료로 기록', '원인 분리·관측 로그 보완·안정된 환경 재측정 필요'])
        if args.comparison_summary:
            set_cell(3, 2, 1, '50명11/5·100명11/6. VU1, FCM대역200ms. 각30요청씩3회, 워밍업 제외')
        with ZipFile(target, 'w') as result:
            for info in original.infolist():
                data = E.tostring(doc, encoding='UTF-8', xml_declaration=True, standalone=True) if info.filename == 'word/document.xml' else original.read(info.filename)
                result.writestr(info, data)
    print(target)


if __name__ == '__main__':
    main()
