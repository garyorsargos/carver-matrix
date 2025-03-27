import { ConfigType } from "../multiMatrixProvider";
import jsPDF from "jspdf";

interface MatrixData {
  config: ConfigType;
  matrixMap: Map<string, Map<string, number>>;
}

export const generateMatrixPdf = ({ config, matrixMap }: MatrixData) => {
  const pdf = new jsPDF("p", "mm", "a4");
  const pageWidth = pdf.internal.pageSize.getWidth();
  const pageHeight = pdf.internal.pageSize.getHeight();
  const margin = 7.5;
  const contentWidth = pageWidth - (margin * 2);

  // First page background (white)
  pdf.setFillColor(255, 255, 255);
  pdf.rect(0, 0, pageWidth, pageHeight, "F");

  // Header
  pdf.setFillColor(1, 64, 147);
  pdf.rect(0, 0, pageWidth, 30, "F");
  
  // Draw analytics icon
  const iconSize = 16;
  const iconX = margin;
  const iconY = 7;
  
  // Draw bars of a bar chart
  pdf.setFillColor(255, 255, 255);
  pdf.setDrawColor(255, 255, 255);
  
  // Bar 1 (shortest)
  pdf.rect(iconX, iconY + iconSize * 0.75, iconSize * 0.2, iconSize * 0.25, "F");
  
  // Bar 2 (medium)
  pdf.rect(iconX + iconSize * 0.4, iconY + iconSize * 0.4, iconSize * 0.2, iconSize * 0.6, "F");
  
  // Bar 3 (tallest)
  pdf.rect(iconX + iconSize * 0.8, iconY, iconSize * 0.2, iconSize, "F");
  
  // Draw trend line
  pdf.setLineWidth(0.5);
  pdf.setDrawColor(255, 255, 255);
  pdf.line(
    iconX, iconY + iconSize * 0.75,
    iconX + iconSize * 0.4, iconY + iconSize * 0.4
  );
  pdf.line(
    iconX + iconSize * 0.4, iconY + iconSize * 0.4,
    iconX + iconSize * 0.8, iconY
  );
  
  pdf.setFontSize(20);
  pdf.setTextColor(255, 255, 255);
  pdf.setFont("helvetica", "bold");
  pdf.text("CARVER Matrix Analysis Report", margin + iconSize + 10, 20);

  // Matrix Info Section
  pdf.setFontSize(20);
  pdf.setTextColor(1, 64, 147);
  pdf.text(config.name, margin, 40);
  pdf.setFontSize(10);
  pdf.setTextColor(0, 0, 0);
  pdf.setFont("helvetica", "normal");
  pdf.text(config.description, margin, 48);

  const categories = [
    "Criticality",
    "Accessibility",
    "Recuperability",
    "Vulnerability",
    "Effect",
    "Recognizability",
  ];

  // Matrix Table
  const tableTop = 55;
  const cellPadding = 5;
  const targetColumnWidth = contentWidth * 0.25;
  const categoryColumnWidth = (contentWidth - targetColumnWidth) / categories.length;
  const rowHeight = 10;

  // Table Header
  pdf.setFillColor(1, 64, 147);
  pdf.rect(margin, tableTop, contentWidth, rowHeight + cellPadding * 2, "F");
  
  // Draw header cell borders
  pdf.setDrawColor(255, 255, 255);
  pdf.setLineWidth(0.3);
  
  // Vertical lines for header
  pdf.line(margin + targetColumnWidth, tableTop, margin + targetColumnWidth, tableTop + rowHeight + cellPadding * 2);
  categories.forEach((_, index) => {
    if (index < categories.length - 1) {
      const xPos = margin + targetColumnWidth + ((index + 1) * categoryColumnWidth);
      pdf.line(xPos, tableTop, xPos, tableTop + rowHeight + cellPadding * 2);
    }
  });

  // Center align the text in each column
  const centerTextInColumn = (text: string, x: number, width: number, y: number) => {
    const textWidth = pdf.getStringUnitWidth(text) * pdf.getFontSize() / pdf.internal.scaleFactor;
    const textX = x + (width - textWidth) / 2;
    pdf.text(text, textX, y);
  };

  // Target header
  pdf.setTextColor(255, 255, 255);
  pdf.setFont("helvetica", "bold");
  pdf.setFontSize(9);
  centerTextInColumn("Target", margin, targetColumnWidth, tableTop + rowHeight);
  
  // Category headers with single letters
  pdf.setFontSize(14);
  const carverLetters = ["C", "A", "R", "V", "E", "R"];
  categories.forEach((_, index) => {
    const xPos = margin + targetColumnWidth + (index * categoryColumnWidth);
    centerTextInColumn(carverLetters[index], xPos, categoryColumnWidth, tableTop + rowHeight);
  });

  // Add page tracking for footer management
  let currentPage = 1;
  let footerAddedToCurrentPage = false;

  const addFooter = () => {
    if (footerAddedToCurrentPage) return;
    
    const footerY = pageHeight - 20;
    pdf.setFontSize(8);
    pdf.setTextColor(128, 128, 128);
    pdf.text(`Generated on ${new Date().toLocaleString()}`, margin, footerY);
    pdf.text("CARVER Matrix Dashboard App", pageWidth - margin - pdf.getStringUnitWidth("CARVER Matrix Dashboard App") * 8 / pdf.internal.scaleFactor, footerY);
    
    footerAddedToCurrentPage = true;
  };

  // Add footer to first page
  addFooter();

  // Table Body
  let currentY = tableTop + rowHeight + cellPadding * 2;
  pdf.setFontSize(9);

  const targets = Array.from(matrixMap.keys()).sort((a, b) => a.localeCompare(b));

  targets.forEach((target, rowIndex) => {
    if (currentY + rowHeight + cellPadding * 2 > pageHeight - 40) {
      addFooter();
      pdf.addPage();
      currentPage++;
      footerAddedToCurrentPage = false;
      currentY = 40;
      
      // Add table header to new page
      pdf.setFillColor(1, 64, 147);
      pdf.rect(margin, currentY - rowHeight - cellPadding * 2, contentWidth, rowHeight + cellPadding * 2, "F");
      
      // Redraw header borders and text
      pdf.setDrawColor(255, 255, 255);
      pdf.setLineWidth(0.3);
      pdf.line(margin + targetColumnWidth, currentY - rowHeight - cellPadding * 2, 
              margin + targetColumnWidth, currentY);
      
      categories.forEach((_, index) => {
        if (index < categories.length - 1) {
          const xPos = margin + targetColumnWidth + ((index + 1) * categoryColumnWidth);
          pdf.line(xPos, currentY - rowHeight - cellPadding * 2, xPos, currentY);
        }
      });

      pdf.setTextColor(255, 255, 255);
      pdf.setFont("helvetica", "bold");
      pdf.setFontSize(9);
      centerTextInColumn("Target", margin, targetColumnWidth, currentY - cellPadding);
      
      pdf.setFontSize(14);
      categories.forEach((_, index) => {
        const xPos = margin + targetColumnWidth + (index * categoryColumnWidth);
        centerTextInColumn(carverLetters[index], xPos, categoryColumnWidth, currentY - cellPadding);
      });
      
      pdf.setFontSize(9);
    }

    // Row background
    pdf.setFillColor(rowIndex % 2 === 0 ? '#f5f5f5' : '#ffffff');
    pdf.rect(margin, currentY, contentWidth, rowHeight + cellPadding * 2, "F");

    // Draw cell borders
    pdf.setDrawColor(200, 200, 200);
    
    if (rowIndex === 0) {
      pdf.setDrawColor(255, 255, 255);
    }
    pdf.line(margin, currentY, margin + contentWidth, currentY);
    
    pdf.setDrawColor(200, 200, 200);
    pdf.line(margin + targetColumnWidth, currentY, margin + targetColumnWidth, currentY + rowHeight + cellPadding * 2);
    categories.forEach((_, index) => {
      if (index < categories.length - 1) {
        const xPos = margin + targetColumnWidth + ((index + 1) * categoryColumnWidth);
        pdf.line(xPos, currentY, xPos, currentY + rowHeight + cellPadding * 2);
      }
    });

    // Target name
    pdf.setTextColor(0, 0, 0);
    pdf.setFont("helvetica", "bold");
    const targetText = target.length > 30 ? target.substring(0, 27) + "..." : target;
    pdf.text(targetText, margin + cellPadding, currentY + rowHeight);

    // Category scores
    pdf.setFont("helvetica", "normal");
    categories.forEach((category, colIndex) => {
      const score = matrixMap.get(target)?.get(category) || 0;
      const xPos = margin + targetColumnWidth + (colIndex * categoryColumnWidth);
      centerTextInColumn(score.toString(), xPos, categoryColumnWidth, currentY + rowHeight);
    });

    currentY += rowHeight + cellPadding * 2;
  });

  addFooter();

  // Rankings Section
  pdf.addPage();
  currentPage++;
  footerAddedToCurrentPage = false;
  pdf.setFillColor(1, 64, 147);
  pdf.rect(0, 0, pageWidth, 30, "F");
  pdf.setFontSize(20);
  pdf.setTextColor(255, 255, 255);
  pdf.setFont("helvetica", "bold");
  pdf.text("Target Rankings", margin, 20);

  pdf.setFontSize(14);
  pdf.setTextColor(1, 64, 147);
  pdf.text("Priority Order", margin, 40);
  pdf.setFontSize(12);
  pdf.setTextColor(0, 0, 0);
  pdf.setFont("helvetica", "normal");

  const categoryToMultiplierMap: { [key: string]: keyof ConfigType } = {
    Criticality: "cMulti",
    Accessibility: "aMulti",
    Recuperability: "rMulti",
    Vulnerability: "vMulti",
    Effect: "eMulti",
    Recognizability: "r2Multi",
  };

  const targetRanks = targets.map(target => {
    const categoriesMap = matrixMap.get(target);
    let totalScore = 0;
    if (categoriesMap) {
      categories.forEach(category => {
        const score = categoriesMap.get(category) || 0;
        const multiplierKey = categoryToMultiplierMap[category];
        if (multiplierKey) {
          const multiplier = config[multiplierKey];
          if (typeof multiplier === "number") {
            totalScore += score * multiplier;
          }
        }
      });
    }
    return { target, totalScore };
  }).sort((a, b) => b.totalScore - a.totalScore);

  let yOffset = 50;
  targetRanks.forEach((item, index) => {
    if (yOffset > pageHeight - 40) {
      addFooter();
      pdf.addPage();
      currentPage++;
      footerAddedToCurrentPage = false;
      pdf.setFillColor(1, 64, 147);
      pdf.rect(0, 0, pageWidth, 30, "F");
      pdf.setFontSize(20);
      pdf.setTextColor(255, 255, 255);
      pdf.text("Target Rankings (continued)", margin, 20);
      yOffset = 50;
    }

    pdf.setTextColor(0, 0, 0);
    pdf.setFont("helvetica", "bold");
    pdf.setFontSize(12);
    pdf.text(`${index + 1}. ${item.target}`, margin, yOffset);
    pdf.setFont("helvetica", "normal");
    pdf.text(`Score: ${item.totalScore.toFixed(2)}`, margin + 100, yOffset);
    yOffset += 10;
  });

  addFooter();

  // Configuration Section
  pdf.addPage();
  currentPage++;
  footerAddedToCurrentPage = false;
  pdf.setFillColor(1, 64, 147);
  pdf.rect(0, 0, pageWidth, 30, "F");
  pdf.setFontSize(20);
  pdf.setTextColor(255, 255, 255);
  pdf.setFont("helvetica", "bold");
  pdf.text("Matrix Configuration", margin, 20);

  pdf.setFontSize(14);
  pdf.setTextColor(1, 64, 147);
  pdf.text("Settings", margin, 40);
  pdf.setFontSize(12);
  pdf.setTextColor(0, 0, 0);
  pdf.setFont("helvetica", "normal");

  const configProperties = [
    { label: "Random Participant Assignment", value: config.randomAssignment ? "Enabled" : "Disabled" },
    { label: "Role Based", value: config.roleBased ? "Enabled" : "Disabled" },
    { label: "Five Point Scoring", value: config.fivePointScoring ? "Enabled" : "Disabled" },
    { label: "Criticality Multiplier", value: config.cMulti },
    { label: "Accessibility Multiplier", value: config.aMulti },
    { label: "Recuperability Multiplier", value: config.rMulti },
    { label: "Vulnerability Multiplier", value: config.vMulti },
    { label: "Effect Multiplier", value: config.eMulti },
    { label: "Recognizability Multiplier", value: config.r2Multi },
  ];

  let configYOffset = 50;
  configProperties.forEach((prop) => {
    if (configYOffset > pageHeight - 40) {
      addFooter();
      pdf.addPage();
      currentPage++;
      footerAddedToCurrentPage = false;
      pdf.setFillColor(1, 64, 147);
      pdf.rect(0, 0, pageWidth, 30, "F");
      pdf.setFontSize(20);
      pdf.setTextColor(255, 255, 255);
      pdf.text("Matrix Configuration (continued)", margin, 20);
      configYOffset = 50;
    }

    pdf.setTextColor(0, 0, 0);
    pdf.setFont("helvetica", "bold");
    pdf.setFontSize(12);
    pdf.text(prop.label, margin, configYOffset);
    pdf.setFont("helvetica", "normal");
    pdf.text(prop.value.toString(), margin + 100, configYOffset);
    configYOffset += 10;
  });

  addFooter();

  const pdfBlob = pdf.output("blob");
  const pdfUrl = URL.createObjectURL(pdfBlob);
  window.open(pdfUrl, "_blank");
}; 