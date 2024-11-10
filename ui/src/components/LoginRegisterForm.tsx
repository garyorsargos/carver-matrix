import React, { useReducer, useState } from "react";
import {
  Box,
  Button,
  TextField,
  Typography,
  Card,
  CircularProgress,
  Alert,
} from "@mui/material";

/**
 * Props interface for the Form component
 * @param mode - Determines whether the form is in 'login' or 'register' mode
 * @param onSubmit - Callback function to handle form submission
 */

type LoginRegisterFormProps = {
  mode: "login" | "register";
  onSubmit: (data: {
    email: string;
    password: string;
    firstName?: string;
    lastName?: string;
  }) => void;
};

/**
 * Interface to define the structure of the form state
 */
type FormState = {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
};

/**
 * Union type defining all possible actions for the form reducer
 */
type ActionType =
  | { type: "SET_FIELD"; field: keyof FormState; value: string }
  | { type: "RESET" };

/**
 * Initial state of the form
 */
const initialState: FormState = {
  firstName: "",
  lastName: "",
  email: "",
  password: "",
};

/**
 * Reducer function to handle form state updates
 * @param state - Current form state
 * @param action - Action to be performed on the state
 * @returns Updated form state
 */
const formReducer = (state: FormState, action: ActionType): FormState => {
  switch (action.type) {
    case "SET_FIELD":
      return { ...state, [action.field]: action.value };
    case "RESET":
      return initialState;
    default:
      return state;
  }
};
/**
 * A reusable form component that handles both login and registration
 * Features:
 * - Form validation
 * - Loading state management
 * - Error handling
 * - Responsive design using Material-UI
 */
const LoginRegisterForm: React.FC<LoginRegisterFormProps> = ({
  mode,
  onSubmit,
}) => {
  // Reducer to manage the form state
  const [state, dispatch] = useReducer(formReducer, initialState);
  // useState hook to manage the loading state
  const [loading, setLoading] = useState(false);
  // State for error message
  const [error, setError] = useState<string | null>(null);

  /**
   * Handles input field changes
   * @param e - Change event from input field
   */
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    dispatch({
      type: "SET_FIELD",
      field: e.target.name as keyof FormState,
      value: e.target.value,
    });
  };

  /**
   * Validates email format using regex
   * @param email - Email string to validate
   * @returns boolean indicating if email is valid
   */
  const validateEmail = (email: string) => /\S+@\S+\.\S+/.test(email);

  /**
   * Handles form submission
   * - Prevents default form behavior
   * - Validates inputs
   * - Manages loading state
   * - Handles submission errors
   * @param e - Form submission event
   */
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!validateEmail(state.email)) {
      setError("Please enter a valid email address.");
      return;
    }
    if (state.password.length < 6) {
      setError("Password should be at least 6 characters long.");
      return;
    }

    setLoading(true); // starts the async operation and enables loading state
    try {
      await onSubmit(state);
      dispatch({ type: "RESET" });
    } catch (err) {
      setError("An error occurred. Please try again.");
    } finally {
      setLoading(false); // disables loading state after operation is completed of fails
    }
  };

  return (
    <Card sx={{ padding: 4, maxWidth: 400, margin: "auto" }}>
      {/* Form title */}
      <Typography variant="h4" sx={{ marginBottom: 2 }}>
        {mode === "login" ? "Login" : "Register"}
      </Typography>

      {/* Error alert */}
      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}
      {/* Form container */}
      <Box
        component="form"
        onSubmit={handleSubmit}
        sx={{ display: "flex", flexDirection: "column", gap: 2 }}
      >
        {/* Conditional rendering of name fields for registration */}
        {mode === "register" && (
          <>
            <TextField
              label="First Name"
              name="firstName"
              value={state.firstName}
              onChange={handleChange}
              required
            />
            <TextField
              label="Last Name"
              name="lastName"
              value={state.lastName}
              onChange={handleChange}
              required
            />
          </>
        )}
        {/* Email field with validation */}
        <TextField
          label="Email"
          name="email"
          type="email"
          value={state.email}
          onChange={handleChange}
          required
          error={Boolean(error) && !validateEmail(state.email)}
          helperText={
            error && !validateEmail(state.email) ? "Invalid email address" : ""
          }
        />

        {/* Password field with validation */}
        <TextField
          label="Password"
          name="password"
          type="password"
          value={state.password}
          onChange={handleChange}
          required
          error={Boolean(error) && state.password.length < 6}
          helperText={
            error && state.password.length < 6 ? "Minimum 6 characters" : ""
          }
        />
        {/**
         * Loading state affects the submit button:
         * - Disables the button during loading
         * - Shows CircularProgress component instead of text
         */}
        <Button
          type="submit"
          variant="contained"
          color="primary"
          disabled={loading}
        >
          {loading ? (
            <CircularProgress size={24} />
          ) : mode === "login" ? (
            "Login"
          ) : (
            "Register"
          )}
        </Button>
      </Box>
    </Card>
  );
};

export default LoginRegisterForm;
