import { Box } from "@mui/material";
import MatrixExplorer from "../components/custom/search/matrixExplorer";
import TargetGroup from "../components/custom/editMatrix/targetGroup";

export const EditMatrix: React.FC = () => {
  return (
    <>
      <Box display="flex" flexDirection="row" justifyContent="space-around" sx={{height:'80vh'}} alignItems="stretch">
        <Box
          id="matrixExplorerBox"
          sx={{
            width: "20%",
            borderRadius: 1,
            backgroundColor: "white",
          }}
        >
          <MatrixExplorer />
        </Box>
        <Box
          id="matrixEditorBox"
          sx={{
            width: "70%",
            borderRadius: 1,
            backgroundColor: "white",
          }}
        >
          <Box id="editMatrixBox">
            <TargetGroup targetName="Example Target 1"/>
          </Box>
          <Box id="userListBox"></Box>
        </Box>
      </Box>
    </>
  );
};

export default EditMatrix;
