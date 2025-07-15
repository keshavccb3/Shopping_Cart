$(function () {
    var $userRegister = $("#userRegister");

    $userRegister.validate({
        rules: {
            name: {
                required: true,
                lettersonly: true
            },
            email: {
                required: true,
                space: true,
                email: true
            },
            mobileNumber: {
                required: true,
                space: true,
                numericOnly: true,
                minlength: 10,
                maxlength: 12
            },
            password: {
                required: true,
                space: true
            },
            confirmpassword: {
                required: true,
                space: true,
                equalTo: '#pass'
            },
            address: {
                required: true,
                all: true
            },
            city: {
                required: true,
            },
            state: {
                required: true
            },
            pincode: {
                required: true,
                space: true,
                numericOnly: true
            },
            img: {
                required: true
            }
        },
        messages: {
            name: {
                required: 'Name is required',
                lettersonly: 'Only letters, spaces, underscores, and hyphens are allowed'
            },
            email: {
                required: 'Email is required',
                space: 'Spaces are not allowed',
                email: 'Invalid email address'
            },
            mobileNumber: {
                required: 'Mobile number is required',
                space: 'Spaces are not allowed',
                numericOnly: 'Only numeric values are allowed',
                minlength: 'Minimum 10 digits',
                maxlength: 'Maximum 12 digits'
            },
            password: {
                required: 'Password is required',
                space: 'Spaces are not allowed'
            },
            confirmpassword: {
                required: 'Confirm password is required',
                space: 'Spaces are not allowed',
                equalTo: 'Passwords do not match'
            },
            address: {
                required: 'Address is required',
                all: 'Invalid address format'
            },
            city: {
                required: 'City is required',
            },
            state: {
                required: 'State is required'
            },
            pincode: {
                required: 'Pincode is required',
                space: 'Spaces are not allowed',
                numericOnly: 'Invalid pincode'
            },
            img: {
                required: 'Image is required'
            }
        }
    });

    var $orders = $("#orders");

    $orders.validate({
        rules: {
            firstName: {
                required: true,
                lettersonly: true
            },
            lastName: {
                required: true,
                lettersonly: true
            },
            email: {
                required: true,
                space: true,
                email: true
            },
            mobileNumber: {
                required: true,
                space: true,
                numericOnly: true,
                minlength: 10,
                maxlength: 12
            },
            address: {
                required: true,
                all: true
            },
            city: {
                required: true,
            },
            state: {
                required: true
            },
            pincode: {
                required: true,
                space: true,
                numericOnly: true
            },
            paymentType: {
                required: true
            }
        },
        messages: {
            firstName: {
                required: 'First name is required',
                lettersonly: 'Only letters, spaces, underscores, and hyphens are allowed'
            },
            lastName: {
                required: 'Last name is required',
                lettersonly: 'Only letters, spaces, underscores, and hyphens are allowed'
            },
            email: {
                required: 'Email is required',
                space: 'Spaces are not allowed',
                email: 'Invalid email address'
            },
            mobileNumber: {
                required: 'Mobile number is required',
                space: 'Spaces are not allowed',
                numericOnly: 'Only numeric values are allowed',
                minlength: 'Minimum 10 digits',
                maxlength: 'Maximum 12 digits'
            },
            address: {
                required: 'Address is required',
                all: 'Invalid address format'
            },
            city: {
                required: 'City is required',
            },
            state: {
                required: 'State is required'
            },
            pincode: {
                required: 'Pincode is required',
                space: 'Spaces are not allowed',
                numericOnly: 'Invalid pincode'
            },
            paymentType: {
                required: 'Please select a payment type'
            }
        }
    });
	
	var $product = $("#product");

	    $product.validate({
	        rules: {
	            title: {
	                required: true,
	                all: true
	            },
	            description: {
	                required: true,
	                all: true
	            },
	            category: {
	                required: true
	            },
	            price: {
	                required: true,
	                number: true,
	                min: 0
	            },
	            isActive: {
	                required: true
	            },
	            stock: {
	                required: true,
	                number: true,
	                min: 0
	            },
	            file: {
	                required: true
	            }
	        },
	        messages: {
	            title: {
	                required: "Title is required",
	                all: "Title contains invalid characters"
	            },
	            description: {
	                required: "Description is required",
	                all: "Description contains invalid characters"
	            },
	            category: {
	                required: "Please select a category"
	            },
	            price: {
	                required: "Price is required",
	                number: "Enter a valid number",
	                min: "Price cannot be negative"
	            },
	            isActive: {
	                required: "Please select a status"
	            },
	            stock: {
	                required: "Stock is required",
	                number: "Enter a valid number",
	                min: "Stock cannot be negative"
	            },
	            file: {
	                required: "Image is required"
	            }
	        }
	    });

    var $resetPassword = $("#resetPassword");

    $resetPassword.validate({
        rules: {
            password: {
                required: true,
                space: true
            },
            confirmPassword: {
                required: true,
                space: true,
                equalTo: '#pass'
            }
        },
        messages: {
            password: {
                required: 'Password is required',
                space: 'Spaces are not allowed'
            },
            confirmPassword: {
                required: 'Confirm password is required',
                space: 'Spaces are not allowed',
                equalTo: 'Passwords do not match'
            }
        }
    });

    var $category = $("#category");

    $category.validate({
        rules: {
            name: {
                required: true,
                lettersonly: true
            },
            isActive: {
                required: true
            },
            file: {
                required: true
            }
        },
        messages: {
            name: {
                required: "Category name is required",
                lettersonly: "Only letters, spaces, underscores, and hyphens are allowed"
            },
            isActive: {
                required: "Please select the status"
            },
            file: {
                required: "Image is required"
            }
        }
    });
});

jQuery.validator.addMethod('lettersonly', function (value, element) {
    return /^[^-\s][a-zA-Z_\s-]+$/.test(value);
});

jQuery.validator.addMethod('space', function (value, element) {
    return /^[^-\s]+$/.test(value);
});

jQuery.validator.addMethod('all', function (value, element) {
    return /^[^-\s][a-zA-Z0-9_,.\s-]+$/.test(value);
});

jQuery.validator.addMethod('numericOnly', function (value, element) {
    return /^[0-9]+$/.test(value);
});
