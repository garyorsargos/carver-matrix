import { Box, Typography, Grid } from "@mui/material";
import MatrixExplorer from "../components/custom/search/matrixExplorer";
import TargetGroup from "../components/custom/editMatrix/targetGroup";

export const EditMatrix: React.FC = () => {
  return (
    <>
      <Box
        display="flex"
        flexDirection="row"
        justifyContent="space-around"
        sx={{ height: "85vh", mt: 2 }}
        alignItems="stretch"
      >
        <Box
          id="matrixExplorerBox"
          sx={{
            width: "22%",
            borderRadius: 1,
            backgroundColor: "white",
            overflowY: "auto",
            p: 1,
          }}
        >
          <MatrixExplorer />
        </Box>
        <Box
          id="matrixEditorBox"
          sx={{
            width: "74%",
            borderRadius: 1,
            backgroundColor: "white",
            overflowY: "auto",
            p: 1,
          }}
        >
          <Box id="editMatrixBox">
            <Typography variant="h2">Matrix Name</Typography>
            <Grid container spacing={2}>
              <Grid item xs={6}>
                <TargetGroup targetName="Example Target 1" />
              </Grid>
              <Grid item xs={6}>
                <TargetGroup targetName="Example Target 2" />
              </Grid>
              <Grid item xs={6}>
                <TargetGroup targetName="Example Target 3" />
              </Grid>
            </Grid>
          </Box>
          <Box id="userListBox"></Box>
        </Box>
      </Box>
    </>
  );
};

export default EditMatrix;
