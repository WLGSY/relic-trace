/**
 * 前端导出与打印工具
 * ------------------------------------------------------------------
 * 统计报表、操作日志的"导出"在前端生成带 BOM 的 UTF-8 CSV，
 * Excel 可直接打开且中文不乱码；单据打印使用独立打印窗口，避免污染页面样式。
 */
export function exportCsv(filename, headers, rows) {
  const escape = (value) => {
    const text = value === null || value === undefined ? '' : String(value)
    return `"${text.replace(/"/g, '""')}"`
  }
  const lines = [headers.map(escape).join(',')]
  rows.forEach((row) => lines.push(row.map(escape).join(',')))

  const blob = new Blob([`\uFEFF${lines.join('\r\n')}`], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename.endsWith('.csv') ? filename : `${filename}.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

/**
 * 打印 HTML 片段（出入库单 / 藏品标签）
 * @param {string} title 文档标题
 * @param {string} html 打印内容
 */
export function printHtml(title, html) {
  const win = window.open('', '_blank', 'width=900,height=1000')
  if (!win) return false
  win.document.write(`<!doctype html>
<html lang="zh-CN"><head><meta charset="utf-8"><title>${title}</title>
<style>
  *{box-sizing:border-box}
  body{font-family:"PingFang SC","Microsoft YaHei",sans-serif;color:#14171a;padding:36px;font-size:13px;line-height:1.8}
  h1{font-size:20px;text-align:center;letter-spacing:.08em;margin:0 0 6px}
  .sub{text-align:center;font-size:11px;color:#6b737b;letter-spacing:.1em;margin-bottom:24px}
  table{width:100%;border-collapse:collapse;margin-top:12px}
  th,td{border:1px solid #c9c9c2;padding:8px 10px;text-align:left;vertical-align:top}
  th{background:#f5f5f1;font-weight:600;width:120px}
  .foot{display:flex;justify-content:space-between;margin-top:36px;font-size:12px;color:#3d444b}
  .qr{width:110px;height:110px;border:6px solid #fff;outline:1px solid #b3b9be;
      background:repeating-linear-gradient(0deg,#14171a 0 5px,transparent 5px 10px),
                 repeating-linear-gradient(90deg,#14171a 0 5px,transparent 5px 10px)}
  .center{text-align:center}
</style></head>
<body>${html}</body></html>`)
  win.document.close()
  win.focus()
  win.print()
  return true
}
