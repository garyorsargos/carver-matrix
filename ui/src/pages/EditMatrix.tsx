import React, { useState, useMemo, useEffect } from "react";
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
  FormControlLabel,
  Checkbox,
} from "@mui/material";
import MatrixExplorer from "../components/custom/search/matrixExplorer";
import CategoryGroup from "../components/custom/editMatrix/categoryGroup";
import {
  MultiMatrixProvider,
  useMultiMatrix,
  ConfigType,
} from "../components/custom/multiMatrixProvider";
import MatrixLoader from "../components/custom/matrixLoader";
import jsPDF from "jspdf";
import html2canvas from "html2canvas";
import axios from "axios";

const EditMatrixContent: React.FC = () => {
  const { config, rawItems } = useMultiMatrix();
  const currentEmail = config.currentUserEmail;
  const [showAllTargets, setShowAllTargets] = useState(false);

  // Determine user roles if roleBased is enabled.
  const isRoleBased = config.roleBased;
  const isHost =
    isRoleBased && currentEmail ? config.hosts?.includes(currentEmail) : false;
  const isParticipant =
    isRoleBased && currentEmail ? config.participants?.includes(currentEmail) : false;

  // Compute displayed items based on role and the checkbox state.
  const displayedItems = useMemo(() => {
    if (isRoleBased) {
      if (isHost && !isParticipant) {
        // Host only: show all.
        return rawItems;
      } else if (isParticipant && !isHost) {
        // Participant only: show only items where currentEmail is in targetUsers.
        return rawItems.filter(
          (item: any) =>
            Array.isArray(item.targetUsers) && item.targetUsers.includes(currentEmail)
        );
      } else if (isHost && isParticipant) {
        // Both: use the checkbox to toggle.
        return showAllTargets
          ? rawItems
          : rawItems.filter(
              (item: any) =>
                Array.isArray(item.targetUsers) && item.targetUsers.includes(currentEmail)
            );
      }
    } else {
      // Not roleBased: use randomAssignment logic.
      return config.randomAssignment
        ? rawItems.filter(
            (item: any) =>
              Array.isArray(item.targetUsers) && item.targetUsers.includes(currentEmail)
          )
        : rawItems;
    }
    return rawItems;
  }, [
    isRoleBased,
    isHost,
    isParticipant,
    rawItems,
    currentEmail,
    config.randomAssignment,
    showAllTargets,
  ]);

  // Debugging: log when displayedItems changes.
  useEffect(() => {
    console.log("showAllTargets:", showAllTargets, "Displayed items count:", displayedItems.length);
  }, [showAllTargets, displayedItems]);

  // Build matrix map and sorted target names.
  const matrixMap = useMemo(() => {
    const map = new Map<string, Map<string, number>>();
    displayedItems.forEach((item: any) => {
      map.set(
        item.itemName,
        new Map([
          ["Criticality", item.criticality],
          ["Accessibility", item.accessibility],
          ["Recuperability", item.recoverability],
          ["Vulnerability", item.vulnerability],
          ["Effect", item.effect],
          ["Recognizability", item.recognizability],
        ])
      );
    });
    return map;
  }, [displayedItems]);

  const targets = useMemo(() => {
    return Array.from(matrixMap.keys()).sort((a, b) => a.localeCompare(b));
  }, [matrixMap]);

  const categories = [
    "Criticality",
    "Accessibility",
    "Recuperability",
    "Vulnerability",
    "Effect",
    "Recognizability",
  ];

  // Determine whether to show the Export PDF button.
  // For roleBased mode:
  //   • Host only: always show.
  //   • Participant only: hide.
  //   • Both: show only if admin mode (showAllTargets) is enabled.
  // For non–roleBased: always show.
  const showExportPdf = useMemo(() => {
    if (!isRoleBased) return true;
    if (isHost && !isParticipant) return true;
    if (isHost && isParticipant) return showAllTargets;
    return false;
  }, [isRoleBased, isHost, isParticipant, showAllTargets]);

  const openPdfInNewTab = async () => {
    const pdf = new jsPDF("p", "mm", "a4");

    pdf.setFillColor(0, 0, 0);
    pdf.rect(0, 0, 210, 15, "F");
    pdf.setFontSize(16);
    pdf.setTextColor(255, 255, 255);
    pdf.text("CARVER Matrix App", 14, 10);

    pdf.setFontSize(14);
    pdf.setTextColor(0, 0, 0);
    pdf.setFont("helvetica", "bold");
    pdf.text(config.name, 14, 30);
    pdf.setFontSize(12);
    pdf.setFont("helvetica", "normal");
    pdf.text(config.description, 14, 40);

    const element = document.getElementById("pdf-content");
    if (element) {
      const canvas = await html2canvas(element, { scale: 2 });
      const imgData = canvas.toDataURL("image/png");
      const pdfWidth = pdf.internal.pageSize.getWidth();
      const pdfHeight = (canvas.height * pdfWidth) / canvas.width;
      pdf.addImage(imgData, "PNG", 14, 50, pdfWidth - 28, pdfHeight);
    }

    pdf.setFontSize(14);
    pdf.setFont("helvetica", "bold");
    pdf.text("Rankings", 14, 140);
    pdf.setFont("helvetica", "normal");
    pdf.setFontSize(12);

    const targetRanks: { target: string; totalScore: number }[] = [];
    const categoryToMultiplierMap: { [key: string]: keyof ConfigType } = {
      Criticality: "cmulti",
      Accessibility: "amulti",
      Recuperability: "rmulti",
      Vulnerability: "vmulti",
      Effect: "emulti",
      Recognizability: "r2Multi",
    };

    matrixMap.forEach((categoriesMap, target) => {
      let totalScore = 0;
      categoriesMap.forEach((score, category) => {
        const multiplierKey = categoryToMultiplierMap[category];
        if (multiplierKey) {
          const multiplier = config[multiplierKey];
          if (typeof multiplier === "number") {
            totalScore += score * multiplier;
          } else {
            console.warn(`Multiplier for ${category} is not a valid number.`);
          }
        }
      });
      targetRanks.push({ target, totalScore });
    });

    targetRanks.sort((a, b) => b.totalScore - a.totalScore);

    let yOffset = 150;
    targetRanks.forEach((item, index) => {
      pdf.text(`${index + 1}. ${item.target}: ${item.totalScore}`, 14, yOffset);
      yOffset += 10;
    });

    pdf.addPage();

    pdf.setFontSize(14);
    pdf.setFont("helvetica", "bold");
    pdf.text("Matrix Config", 14, 20);
    pdf.setFont("helvetica", "normal");
    pdf.setFontSize(12);

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

    const pdfBlob = pdf.output("blob");
    const pdfUrl = URL.createObjectURL(pdfBlob);
    window.open(pdfUrl, "_blank");
  };

  const handleSubmitUpdates = () => {
    const params = new URLSearchParams(window.location.search);
    const matrixId = params.get("matrixId");
    if (!matrixId) {
      console.error("matrixId query parameter is missing.");
      return;
    }
    axios
      .put(`/api/carvermatrices/${matrixId}/carveritems/update`, {})
      .then((response) => {
        console.log("Updates submitted successfully", response);
      })
      .catch((error) => {
        console.error("Error submitting updates", error);
      });
  };

  return (
    <Box display="flex" flexDirection="row" sx={{ height: "85vh", mt: 2, gap: 2, p: 2, borderRadius: 2 }}>
      <MatrixLoader />
      <Box
        id="matrixExplorerBox"
        sx={{
          width: "20%",
          minWidth: "200px",
          backgroundColor: "#ffffff",
          overflowY: "auto",
          p: 1,
          flexShrink: 0,
          borderRadius: 1,
        }}
      >
        <MatrixExplorer />
      </Box>
      <Box
        id="matrixEditorBox"
        sx={{
          flexGrow: 1,
          backgroundColor: "#ffffff",
          overflowY: "auto",
          p: 1,
          display: "flex",
          flexDirection: "column",
          minWidth: 0,
          position: "relative",
          borderRadius: 1,
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
            {config.name || ""}
          </Typography>

          {isRoleBased && isHost && isParticipant && (
            <FormControlLabel
              control={
                <Checkbox
                  checked={showAllTargets}
                  onChange={(e) => setShowAllTargets(e.target.checked)}
                />
              }
              label="Admin View"
            />
          )}

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
                  <TableCell align="center" sx={{ fontWeight: "bold", color: "white" }}>
                    Targets
                  </TableCell>
                  {categories.map((category) => (
                    <TableCell key={category} align="center" sx={{ fontWeight: "bold", color: "white" }}>
                      {category}
                    </TableCell>
                  ))}
                </TableRow>
              </TableHead>
              <TableBody>
                {targets.map((target, index) => (
                  <TableRow
                    key={target}
                    sx={{ backgroundColor: index % 2 === 0 ? "#ffffff" : "#f5f5f5" }}
                  >
                    <TableCell align="center" sx={{ color: "black" }}>
                      {target}
                    </TableCell>
                    {categories.map((category) => (
                      <TableCell key={`${target}-${category}`} align="center" sx={{ color: "black" }}>
                        <CategoryGroup category={category} targetTitle={target} />
                      </TableCell>
                    ))}
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </div>

        <Box sx={{ position: "absolute", bottom: 16, right: 16, zIndex: 10 }}>
          {showExportPdf && (
            <Button variant="contained" color="primary" sx={{ mr: 2 }} onClick={openPdfInNewTab}>
              Export PDF
            </Button>
          )}
          <Button variant="contained" color="success" onClick={handleSubmitUpdates}>
            Submit
          </Button>
        </Box>
      </Box>
    </Box>
  );
};

export const EditMatrix: React.FC = () => {
  return (
    <MultiMatrixProvider>
      <EditMatrixContent />
    </MultiMatrixProvider>
  );
};

export default EditMatrix;

