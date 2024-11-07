import { Box, Typography } from "@mui/material";
import FiltersInterface from "../components/custom/search/filtersInterface";
import MatrixExplorer from "../components/custom/search/matrixExplorer";

export const EditMatrix: React.FC = () => {
  return (
    <>
      <Typography data-testid="edit-page" sx={{ color: "#FFF" }}>
        This is the edit page.
      </Typography>
      <Box id="searchFiltersBox">
        <FiltersInterface/>
      </Box>
      <Box id="matrixExplorerBox">
        <MatrixExplorer/>
      </Box>
      <Box id="matrixEditorBox">
        <Box id="userListBox">

        </Box>
        <Box id="editMatrixBox">

        </Box>
      </Box>
    </>
  );
};

export default EditMatrix;
