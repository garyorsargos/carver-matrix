import { Box, Typography, Grid, Button } from "@mui/material";
import MatrixExplorer from "../components/custom/search/matrixExplorer";
import CategoryGroup from "../components/custom/editMatrix/categoryGroup";
import { MultiMatrixProvider } from "../components/custom/multiMatrixProvider";

export const EditMatrix: React.FC = () => {
  const categories = [
    "Criticality",
    "Accessibility",
    "Recuperability",
    "Vulnerability",
    "Effect",
    "Recognizability"
  ];

  const targets = ["Example Target 1", "Example Target 2", "Example Target 3"];

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
            backgroundColor: "white",
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
            backgroundColor: "white",
            overflowY: "auto",
            p: 1,
            display: "flex",
            flexDirection: "column",
            minWidth: 0,
            position: "relative"  // This allows absolute positioning of the button inside the box
          }}
        >
          <Box id="editMatrixBox" sx={{ px: 0.5, py: 0.5 }}>
            <Typography variant="h4" align="center" sx={{ fontSize: { xs: "1.5rem", md: "2rem" }, mb: 2 }}>
              Matrix Name
            </Typography>
            <Grid container spacing={0.5} sx={{ width: "100%", overflowX: "auto" }}>
              <Grid item xs={12}>
                <Grid container spacing={0.5} sx={{ width: "100%" }}>
                  <Grid item xs={1.7} sx={{ padding: 0.5, display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
                    <Typography variant="h6" sx={{ fontSize: { xs: "0.75rem", md: "1rem" }, fontWeight: 'bold' }}>
                      Targets
                    </Typography>
                  </Grid>
                  {categories.map((category) => (
                    <Grid item xs={1.7} sx={{ border: "1px solid black", padding: 0.5 }} key={category}>
                      <Typography variant="h6" align="center" sx={{ fontSize: { xs: "0.75rem", md: "1rem" }, fontWeight: 'bold' }}>
                        {category}
                      </Typography>
                    </Grid>
                  ))}
                </Grid>
              </Grid>
              {targets.map((target) => (
                <Grid item xs={12} key={target}>
                  <Grid container spacing={0.5} alignItems="center" sx={{ width: "100%" }}>
                    <Grid item xs={1.7} sx={{ padding: 0.5, height: "100%" }}>
                      <Typography variant="h6" align="center" sx={{ fontSize: { xs: "0.75rem", md: "1rem" } }}>
                        {target}
                      </Typography>
                    </Grid>
                    {categories.map((category) => (
                      <Grid item xs={1.7} key={`${target}-${category}`} display="flex" justifyContent="center" sx={{ border: "1px solid black", padding: 0.5 }}>
                        <CategoryGroup category={category} targetTitle={target} />
                      </Grid>
                    ))}
                  </Grid>
                </Grid>
              ))}
            </Grid>
          </Box>
          
          <Button
            variant="contained"
            color="success"
            sx={{
              position: "absolute",
              bottom: 16,
              right: 16,
              zIndex: 10
            }}
          >
            Submit
          </Button>
          
          <Box id="userListBox"></Box>
        </Box>
      </Box>
    </MultiMatrixProvider>
  );
};

export default EditMatrix;

