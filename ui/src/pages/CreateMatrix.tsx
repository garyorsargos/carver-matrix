import { Box, Typography } from "@mui/material";

export const CreateMatrix: React.FC = () => {
  return (
    <Box
      sx={{
        backgroundColor: 'lightgray',
        height: '100vh',
        width: '100vw',
        display: 'flex',
        justifyContent: 'left',
        alignItems: 'flex-start',
        p: 2
      }}
    >
      <Box
        id='matrixCreatorBox'
        sx={{
          backgroundColor: 'white',
          p: 2,
          borderRadius: 2,
          width: '95%',
          height: '85%',
          boxShadow: 3,
          position: 'center',
          top: '10px',
          display: 'flex',
        }}
      >
        <Typography variant="h3">Example Matrix Title</Typography>
      </Box>
    </Box>
  );
};

export default CreateMatrix;