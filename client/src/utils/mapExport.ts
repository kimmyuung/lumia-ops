import { jsPDF } from 'jspdf'
import pptxgen from 'pptxgenjs'

export type ImageFormat = 'png' | 'jpeg'
export type ExportFormat = 'png' | 'jpeg' | 'pdf' | 'pptx'

/**
 * Canvas를 이미지로 내보내기
 */
export function exportAsImage(
    canvas: HTMLCanvasElement,
    format: ImageFormat = 'png',
    filename?: string
): void {
    const mimeType = format === 'jpeg' ? 'image/jpeg' : 'image/png'
    const extension = format === 'jpeg' ? 'jpg' : 'png'
    const name = filename || `strategy-map-${Date.now()}`

    const link = document.createElement('a')
    link.download = `${name}.${extension}`
    link.href = canvas.toDataURL(mimeType, 0.95)
    link.click()
}

/**
 * Canvas를 PDF로 내보내기
 */
export function exportAsPDF(
    canvas: HTMLCanvasElement,
    title?: string,
    filename?: string
): void {
    const imgData = canvas.toDataURL('image/png')
    const name = filename || `strategy-map-${Date.now()}`

    // 캔버스 비율에 맞춰 PDF 크기 계산
    const aspectRatio = canvas.width / canvas.height
    const pdfWidth = 297 // A4 landscape width in mm
    const pdfHeight = pdfWidth / aspectRatio

    const pdf = new jsPDF({
        orientation: aspectRatio > 1 ? 'landscape' : 'portrait',
        unit: 'mm',
        format: [pdfWidth, pdfHeight]
    })

    // 제목 추가 (있는 경우)
    if (title) {
        pdf.setFontSize(16)
        pdf.text(title, 10, 15)
        pdf.addImage(imgData, 'PNG', 0, 20, pdfWidth, pdfHeight - 20)
    } else {
        pdf.addImage(imgData, 'PNG', 0, 0, pdfWidth, pdfHeight)
    }

    pdf.save(`${name}.pdf`)
}

/**
 * Canvas를 PPTX로 내보내기
 */
export function exportAsPPTX(
    canvas: HTMLCanvasElement,
    title?: string,
    filename?: string
): void {
    const imgData = canvas.toDataURL('image/png')
    const name = filename || `strategy-map-${Date.now()}`

    const pptx = new pptxgen()
    pptx.author = 'Lumia Ops'
    pptx.title = title || 'Strategy Map'
    pptx.subject = 'Eternal Return Strategy'

    const slide = pptx.addSlide()

    // 제목 추가
    if (title) {
        slide.addText(title, {
            x: 0.5,
            y: 0.3,
            w: '90%',
            h: 0.5,
            fontSize: 24,
            bold: true,
            color: '333333'
        })
    }

    // 이미지 추가 (슬라이드에 맞게 조정)
    slide.addImage({
        data: imgData,
        x: 0.5,
        y: title ? 1 : 0.5,
        w: 9,
        h: title ? 5.5 : 6.5
    })

    pptx.writeFile({ fileName: `${name}.pptx` })
}

/**
 * 통합 내보내기 함수
 */
export function exportMap(
    canvas: HTMLCanvasElement,
    format: ExportFormat,
    title?: string,
    filename?: string
): void {
    switch (format) {
        case 'png':
        case 'jpeg':
            exportAsImage(canvas, format, filename)
            break
        case 'pdf':
            exportAsPDF(canvas, title, filename)
            break
        case 'pptx':
            exportAsPPTX(canvas, title, filename)
            break
    }
}
