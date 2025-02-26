import React from "react";
import {
  Box,
  Typography,
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
} from "@mui/material";
import MatrixExplorer from "../components/custom/search/matrixExplorer";
import CategoryGroup from "../components/custom/editMatrix/categoryGroup";
import {
  MultiMatrixProvider,
  useMultiMatrix,
} from "../components/custom/multiMatrixProvider";
import MatrixLoader from "../components/custom/matrixLoader";
import jsPDF from "jspdf";
import html2canvas from "html2canvas";
import { ConfigType } from "../components/custom/multiMatrixProvider";

// This component contains the content of the page and uses our custom hook.
const EditMatrixContent: React.FC = () => {
  const { multiMatrix, config } = useMultiMatrix();

  console.log(config);

  // Dynamically read targets from the multiMatrix keys.
  const targets = Array.from(multiMatrix.keys());
  const categories = [
    "Criticality",
    "Accessibility",
    "Recuperability",
    "Vulnerability",
    "Effect",
    "Recognizability",
  ];

  // This function generates the PDF
  const openPdfInNewTab = async () => {
    const pdf = new jsPDF("p", "mm", "a4");

    // Add page header (CARVER Matrix App)
    pdf.setFillColor(0, 0, 0); // Black background
    pdf.rect(0, 0, 210, 15, 'F'); // Full-width black rectangle
    pdf.setFontSize(16);
    pdf.setTextColor(255, 255, 255); // White text
    pdf.text("CARVER Matrix App", 14, 10);

    // Add Matrix Title and Description
    pdf.setFontSize(14);
    pdf.setTextColor(0, 0, 0); // Black text
    pdf.setFont("helvetica", "bold");
    pdf.text(config.name, 14, 30); // Replace with actual title
    pdf.setFontSize(12);
    pdf.setFont("helvetica", "normal");
    pdf.text(config.description, 14, 40);

    // Add the matrix image
    const element = document.getElementById("pdf-content");
    if (element) {
      const canvas = await html2canvas(element, { scale: 2 });
      const imgData = canvas.toDataURL("image/png");
      const pdfWidth = pdf.internal.pageSize.getWidth();
      const pdfHeight = (canvas.height * pdfWidth) / canvas.width;
      pdf.addImage(imgData, "PNG", 14, 50, pdfWidth - 28, pdfHeight);
    }


    // Add Rankings Section
    pdf.setFontSize(14);
    pdf.setFont("helvetica", "bold");
    pdf.text("Rankings", 14, 140);
    pdf.setFont("helvetica", "normal");
    pdf.setFontSize(12);

    // Calculate rankings
    const targetRanks: { target: string; totalScore: number }[] = [];
    const categoryToMultiplierMap: { [key: string]: keyof ConfigType } = {
      Criticality: 'cmulti',
      Accessibility: 'amulti',
      Recuperability: 'rmulti',
      Vulnerability: 'vmulti',
      Effect: 'emulti',
      Recognizability: 'r2Multi',
    };

    multiMatrix.forEach((categoriesMap, target) => {
      let totalScore = 0;
      categoriesMap.forEach((score, category) => {
        const multiplierKey = categoryToMultiplierMap[category]; // Use the map to get the multiplier key
        if (multiplierKey) {
          const multiplier = config[multiplierKey]; // Now access the multiplier directly
          if (typeof multiplier === "number") {
            totalScore += score * multiplier;
          } else {
            console.warn(`Multiplier for ${category} is not a valid number.`);
          }
        }
      });
      targetRanks.push({ target, totalScore });
    });

    // Sort by total score (descending)
    targetRanks.sort((a, b) => b.totalScore - a.totalScore);

    // List the targets with their ranking
    let yOffset = 150;
    targetRanks.forEach((item, index) => {
      pdf.text(`${index + 1}. ${item.target}: ${item.totalScore}`, 14, yOffset);
      yOffset += 10;
    });

    // Add another page break
    pdf.addPage();

    // Add Matrix Config Section
    pdf.setFontSize(14);
    pdf.setFont("helvetica", "bold");
    pdf.text("Matrix Config", 14, 20);
    pdf.setFont("helvetica", "normal");
    pdf.setFontSize(12);

    // List the config properties
    const configProperties = [
      { label: "Random Participant Assignment", value: config.randomAssignment ? "Yes" : "No" },
      { label: "Role Based", value: config.roleBased ? "Yes" : "No" },
      { label: "Five Point Scoring", value: config.fivePointScoring ? "Yes" : "No" },
      { label: "Criticality Multiplier", value: config.cmulti },
      { label: "Accessibility Multiplier", value: config.amulti },
      { label: "Recuperability Multiplier", value: config.rmulti },
      { label: "Vulnerability Multiplier", value: config.vmulti },
      { label: "Effect Multiplier", value: config.emulti },
      { label: "Recognizability Multiplier", value: config.r2Multi },

    ];

    let configYOffset = 30;
    configProperties.forEach((prop) => {
      pdf.text(`${prop.label}: ${prop.value}`, 14, configYOffset);
      configYOffset += 10;
    });

    // Open the generated PDF in a new tab
    const pdfBlob = pdf.output("blob");
    const pdfUrl = URL.createObjectURL(pdfBlob);
    window.open(pdfUrl, "_blank");
  };

  return (
    <Box
      display="flex"
      flexDirection="row"
      sx={{ height: "85vh", mt: 2, gap: 2 }}
    >
      <MatrixLoader />
      <Box
        id="matrixExplorerBox"
        sx={{
          width: "20%",
          minWidth: "200px",
          borderRadius: 1,
          backgroundColor: "#f5f5f5",
          overflowY: "auto",
          p: 1,
          flexShrink: 0,
        }}
      >
        <MatrixExplorer />
      </Box>
      <Box
        id="matrixEditorBox"
        sx={{
          flexGrow: 1,
          borderRadius: 1,
          backgroundColor: "#ffffff",
          overflowY: "auto",
          p: 1,
          display: "flex",
          flexDirection: "column",
          minWidth: 0,
          position: "relative",
        }}
      >
        <div id="pdf-content">
          <Typography
            variant="h4"
            align="center"
            sx={{
              fontSize: { xs: "1.5rem", md: "2rem" },
              mb: 2,
              color: "black",
            }}
          >
            {config.name || "Matrix Name"}
          </Typography>

          <TableContainer
            component={Paper}
            sx={{
              maxHeight: "65vh",
              overflowY: "auto",
              backgroundColor: "#f9f9f9",
            }}
          >
            <Table stickyHeader>
              <TableHead>
                <TableRow sx={{ backgroundColor: "#e0e0e0" }}>
                  <TableCell
                    align="center"
                    sx={{ fontWeight: "bold", color: "white" }}
                  >
                    Targets
                  </TableCell>
                  {categories.map((category) => (
                    <TableCell
                      key={category}
                      align="center"
                      sx={{ fontWeight: "bold", color: "white" }}
                    >
                      {category}
                    </TableCell>
                  ))}
                </TableRow>
              </TableHead>
              <TableBody>
                {targets.map((target, index) => (
                  <TableRow
                    key={target}
                    sx={{
                      backgroundColor: index % 2 === 0 ? "#ffffff" : "#f5f5f5",
                    }}
                  >
                    <TableCell align="center" sx={{ color: "black" }}>
                      {target}
                    </TableCell>
                    {categories.map((category) => (
                      <TableCell
                        key={`${target}-${category}`}
                        align="center"
                        sx={{ color: "black" }}
                      >
                        <CategoryGroup
                          category={category}
                          targetTitle={target}
                        />
                      </TableCell>
                    ))}
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </div>

        <Button
          variant="contained"
          color="success"
          sx={{ position: "absolute", bottom: 16, right: 16, zIndex: 10 }}
        >
          Submit
        </Button>

        <Button
          variant="contained"
          color="primary"
          sx={{ position: "absolute", bottom: 16, right: 100, zIndex: 10 }}
          onClick={openPdfInNewTab}
        >
          Export PDF
        </Button>
      </Box>
    </Box>
  );
};

// The main page component wraps the content with the provider.
export const EditMatrix: React.FC = () => {
  return (
    <MultiMatrixProvider>
      <EditMatrixContent />
    </MultiMatrixProvider>
  );
};

export default EditMatrix;

