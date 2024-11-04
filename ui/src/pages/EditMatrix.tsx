import { Box, Typography } from "@mui/material";

export const EditMatrix: React.FC = () => {
  return (
    <>
      <Typography data-testid="edit-page" sx={{ color: "#FFF" }}>
        This is the edit page.
      </Typography>
      <Box id="searchFiltersBox">

      </Box>
      <Box id="matrixExplorerBox">

      </Box>
      <Box id="matrixEditorBox">

      </Box>
    </>
  );
};

export default EditMatrix;
