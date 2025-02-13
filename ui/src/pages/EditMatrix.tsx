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
import { MultiMatrixProvider } from "../components/custom/multiMatrixProvider";
import jsPDF from 'jspdf';
import html2canvas from 'html2canvas';

export const EditMatrix: React.FC = () => {
  const categories = [
    "Criticality",
    "Accessibility",
    "Recuperability",
    "Vulnerability",
    "Effect",
    "Recognizability",
  ];

  const targets = ["Example Target 1", "Example Target 2", "Example Target 3"];

  const openPdfInNewTab = async () => {
    const element = document.getElementById('pdf-content');
    if (element) {
      const canvas = await html2canvas(element);
      const imgData = canvas.toDataURL('image/png');
      const pdf = new jsPDF('p', 'mm', 'a4');
      const pdfWidth = pdf.internal.pageSize.getWidth();
      const pdfHeight = (canvas.height * pdfWidth) / canvas.width;
      pdf.addImage(imgData, 'PNG', 0, 0, pdfWidth, pdfHeight);
      const pdfBlob = pdf.output('blob');
      const pdfUrl = URL.createObjectURL(pdfBlob);
      window.open(pdfUrl, '_blank');
    }
  };

  return (
    <MultiMatrixProvider>
      <Box
        display="flex"
        flexDirection="row"
        sx={{ height: "85vh", mt: 2, gap: 2 }}
      >
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
              Matrix Name
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
                      sx={{
                        fontWeight: "bold",
                        backgroundColor: "#e0e0e0",
                        color: "black",
                      }}
                    >
                      Targets
                    </TableCell>
                    {categories.map((category) => (
                      <TableCell
                        key={category}
                        align="center"
                        sx={{
                          fontWeight: "bold",
                          backgroundColor: "#e0e0e0",
                          color: "black",
                        }}
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
            Open PDF
          </Button>
        </Box>
      </Box>
    </MultiMatrixProvider>
  );
};

export default EditMatrix;

